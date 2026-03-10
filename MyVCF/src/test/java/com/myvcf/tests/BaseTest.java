package com.myvcf.tests;

import com.myvcf.core.DriverFactory;
import com.myvcf.core.ConfigReader;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@org.testng.annotations.Listeners({
        com.myvcf.listeners.ExtentTestListener.class,
        com.myvcf.listeners.ScreenshotListener.class
})
public abstract class BaseTest {

    // Main WebDriver used by each test class.
    protected WebDriver driver;

    // Explicit wait used by helper methods in this base test.
    private WebDriverWait wait;
    
    // Keeps the current scenario name per thread.
    // Useful for data-driven tests and reporting.
    public static final ThreadLocal<String> CURRENT_SCENARIO = new ThreadLocal<>();

    /** STEP counter (Thread-safe, resets automatically on test start + manually per data record) */
    // Step counter used for logging actions in order inside the report.
    private static final ThreadLocal<Integer> STEP_NO = ThreadLocal.withInitial(() -> 1);

    // ============================================================
    //   1. Setup & Teardown
    // ============================================================

    // Starts the browser before each test method.
    // Also resolves browser, headless mode, and wait time from params/config.
    @Parameters({"browser", "headless", "waitSeconds"})
    @BeforeMethod(alwaysRun = true)
    public void setupDriver(@Optional("chrome") String browser,
                            @Optional("false") String headless,
                            @Optional("10") String waitSecondsParam) {

        STEP_NO.set(1); // ensure fresh STEP numbers each test start

        final String resolvedBrowser =
                (browser == null || browser.isBlank()) ? ConfigReader.get("browser", "chrome") : browser;

        final boolean resolvedHeadless =
                (headless == null || headless.isBlank())
                        ? ConfigReader.getBoolean("headless", false)
                        : Boolean.parseBoolean(headless);

        long seconds;
        try {
            seconds = Long.parseLong(waitSecondsParam);
        } catch (Exception e) {
            seconds = ConfigReader.getLong("wait.defaultSeconds", 10L);
        }

        System.out.println("[BaseTest] Starting WebDriver -> browser=" + resolvedBrowser + ", headless=" + resolvedHeadless);
        DriverFactory.create(resolvedBrowser, resolvedHeadless);
        driver = DriverFactory.getDriver();

        // Small implicit wait plus page-level explicit wait setup
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));

        // Resolve default base URL from config so the run knows what environment is being used
        String baseKey = ConfigReader.get("baseUrl", "EP.SIT").trim();
        String defaultUrl = ConfigReader.get(baseKey);

        if (defaultUrl != null && !defaultUrl.isBlank()) {
            System.out.println("[BaseTest] Default base URL key=" + baseKey + " → " + defaultUrl);
        } else {
            System.err.println("[BaseTest] No valid base URL for key '" + baseKey + "'");
        }
    }

    // Runs after each test method.
    // Handles skip screenshots, cleanup logs, and always closes the browser.
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.SKIP) {
                captureScreenshot(result.getMethod().getMethodName() + "__SKIP");
            }
            try {
                System.out.println("[BaseTest] Final URL: " + driver.getCurrentUrl());
            } catch (Exception ignored) {}
        } finally {
            try {
                if (driver != null) driver.quit();
            } catch (Exception e) {
                System.err.println("[BaseTest] Error during driver.quit(): " + e.getMessage());
            } finally {
                STEP_NO.remove();
                driver = null;
                try { DriverFactory.quitDriver(); } catch (Exception ignored) {}
            }
        }
    }

    // ============================================================
    //   2. Logging to Extent & TestNG
    // ============================================================

    /** Log INFO step */
    // Logs a regular step as INFO in both TestNG output and Extent report.
    protected void step(String message) {
        int n = STEP_NO.get();
        String line = String.format("[STEP %02d] %s", n, message);
        Reporter.log(line, true);
        var t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t != null) t.log(com.aventstack.extentreports.Status.INFO, line);
        STEP_NO.set(n + 1);
    }

    /** Log PASS step */
    // Logs a successful step as PASS in both TestNG output and Extent report.
    protected void passStep(String message) {
        int n = STEP_NO.get();
        String line = String.format("[STEP %02d] %s", n, message);
        Reporter.log(line, true);
        var t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t != null) t.log(com.aventstack.extentreports.Status.PASS, line);
        STEP_NO.set(n + 1);
    }
             

    /** Reset steps for each data-driven validation scenario */
    // Resets the step counter back to 1.
    // Mainly useful when the same test method runs multiple records.
    public static void resetStepCounter() {
        STEP_NO.set(1);
    }

    /** Attach screenshot inline */
    // Captures a screenshot as Base64 and embeds it directly into the Extent report.
    protected void attachScreenshotToExtent(String caption, String nameHint) {
        var t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t == null) return;
        try {
            String base64 = captureScreenshotBase64();
            if (base64 != null) {
                String cap = (caption == null || caption.isBlank())
                        ? (nameHint == null || nameHint.isBlank() ? "Screenshot" : nameHint)
                        : caption;
                t.info(cap,
                        com.aventstack.extentreports.MediaEntityBuilder
                                .createScreenCaptureFromBase64String(base64, cap)
                                .build());
            }
        } catch (Exception e) {
            t.warning("Could not embed screenshot: " + e.getMessage());
        }
    }

    // Logs an informational message without increasing the step counter.
    protected void stepInfo(String message) {
        Reporter.log(message, true);
        var t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t != null) t.log(com.aventstack.extentreports.Status.INFO, message);
    }

    // ============================================================
    //   3. Screenshot Utilities
    // ============================================================

    // Takes a screenshot, saves it under target/screenshots, and returns the file.
    protected File captureScreenshot(String nameHint) {
        if (driver == null) return null;
        try {
            WebDriver snapDriver = driver;
            if (!(snapDriver instanceof TakesScreenshot)) {
                try { snapDriver = new org.openqa.selenium.remote.Augmenter().augment(driver); }
                catch (Exception ignored) {}
            }
            if (!(snapDriver instanceof TakesScreenshot)) return null;

            String baseName = (nameHint == null || nameHint.isBlank()) ? "screenshot" : nameHint;
            baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
            String fileName = baseName + "__" + stamp + ".png";

            File dir = new File(System.getProperty("user.dir"), "target/screenshots");
            Files.createDirectories(dir.toPath());

            File dest = new File(dir, fileName);
            byte[] png = ((TakesScreenshot) snapDriver).getScreenshotAs(OutputType.BYTES);
            Files.write(dest.toPath(), png);

            String href = "file:///" + dest.getAbsolutePath().replace("\\", "/");
            Reporter.log("<a href='" + href + "'>[screenshot]</a>", true);

            System.out.println("[BaseTest] Screenshot saved: " + dest.getAbsolutePath());
            return dest;
        } catch (Exception e) {
            System.err.println("[BaseTest] captureScreenshot failed: " + e.getMessage());
            return null;
        }
    }

    // Takes a screenshot and returns it as Base64 instead of saving a file.
    // Mostly used when we want to embed it straight into the report.
    protected String captureScreenshotBase64() {
        if (driver == null) return null;
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.err.println("[BaseTest] captureScreenshotBase64 failed: " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    //   4. Navigation Helpers
    // ============================================================

    // Resolves the actual base URL from config using the configured base key.
    protected String resolvedBaseUrlFromConfig() {
        String baseKey = ConfigReader.get("baseUrl", "EP.QA").trim();
        String url = ConfigReader.get(baseKey);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("[BaseTest] Missing base URL key: " + baseKey);
        }
        return url.trim();
    }

    // Opens the default base URL only if the browser is still on a blank/about/data page.
    protected void navigateToBaseUrl() {
        try {
            if (driver == null) return;
            String current = "";
            try { current = driver.getCurrentUrl(); } catch (Exception ignored) {}
            boolean looksBlank = (current == null || current.isBlank()
                    || current.startsWith("data:")
                    || current.startsWith("about:"));
            if (looksBlank) {
                String url = resolvedBaseUrlFromConfig();
                driver.get(url);
                stepInfo("Open default base URL → " + url);
            }
        } catch (Exception e) {
            System.err.println("[BaseTest] navigateToBaseUrl failed: " + e.getMessage());
        }
    }

    // Wrapper method in case tests want a clearer method name.
    protected void navigateToBaseUrlIfNeeded() {
        navigateToBaseUrl();
    }

    // Opens the exact URL passed in and logs it as a passed step.
    protected void openUrl(String url) {
        if (driver == null) throw new IllegalStateException("[BaseTest] Driver not initialized.");
        driver.get(url);
        passStep("Navigated to: " + url);
    }

    @Deprecated
    // Old helper that maps environment names to hardcoded URLs.
    // Kept for backward compatibility, but config-based URL handling is preferred.
    protected void openEnvironment(String env) {
        String url;
        switch (env.toLowerCase()) {
            case "uat" -> url = "https://vcf--uat.sandbox.my.salesforce.com/";
            case "sit" -> url = "https://vcf--sit.sandbox.my.salesforce.com/";
            case "qa"  -> url = "https://vcf--qa.sandbox.my.salesforce.com/";
            case "preprod", "pre-prod", "pre_prod" -> url = "https://vcf--preprod.sandbox.my.salesforce.com/";
            default -> throw new IllegalArgumentException("Unknown environment: " + env);
        }
        openUrl(url);
    }

    // ============================================================
    //   5. Explicit Wait Helpers
    // ============================================================

    // Returns the explicit wait object used by this test.
    protected WebDriverWait explicitWait() { return wait; }

    // Waits until the element becomes visible.
    protected WebElement waitVisible(By locator) {
        return explicitWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Waits until the element becomes clickable.
    protected WebElement waitClickable(By locator) {
        return explicitWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Waits until the page title contains the expected text.
    protected void waitTitleContains(String text) {
        explicitWait().until(ExpectedConditions.titleContains(text));
    }

    // Waits until the browser reports that the page is fully loaded.
    protected void waitForDomReady() {
        explicitWait().until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }
}