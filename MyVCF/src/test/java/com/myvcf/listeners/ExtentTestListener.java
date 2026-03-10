
package com.myvcf.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.testng.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.nio.file.Files;
import java.util.Base64;
import com.myvcf.tests.BaseTest;

public class ExtentTestListener implements ITestListener, IConfigurationListener {

    // Runs once before any tests start.
    // Initializes the Extent report instance.
    @Override
    public void onStart(ITestContext context) {
        com.myvcf.reporting.ExtentReportManager.getExtent();
    }

    // Runs after all tests finish.
    // Flushes the report so the HTML file gets written and prints the report location.
    @Override
    public void onFinish(ITestContext context) {
        com.myvcf.reporting.ExtentReportManager.flush();

        String path = com.myvcf.reporting.ExtentReportManager.getReportPath();
        if (path != null && !path.isBlank()) {
            System.out.println("[Extent] Report: " + path);
            String href = "file:///" + path.replace("\\", "/");
            Reporter.log("Extent report: <a href='" + href + "'>" + path + "</a>", true);
        } else {
            System.out.println("[Extent] Report path not set (no tests started?)");
            Reporter.log("Extent report path not set (no tests started?)", true);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {

        // Read scenario stored in ThreadLocal (usually set by the test or page object)
        String scenarioFromTL = com.myvcf.tests.BaseTest.CURRENT_SCENARIO.get();

        // Default placeholders for labels used in the report
        String idLabel = "";
        String scenarioLabel = "";

        // If the test used parameters (DataProvider), try extracting scenario info
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            Object row = params[0];
            scenarioLabel = extractScenario(row);
        }

        // If scenario wasn't found in parameters, fallback to the test description
        if (scenarioLabel == null || scenarioLabel.isBlank()) {
            String desc = result.getMethod().getDescription();
            if (desc != null && !desc.isBlank()) {
                scenarioLabel = desc.trim();
            }
        }

        // Build the display name used in the report
        String methodName = result.getMethod().getMethodName();
        String displayName = String.format("%s [%s]",
                methodName,
                (scenarioFromTL != null && !scenarioFromTL.isBlank())
                        ? scenarioFromTL
                        : scenarioLabel);

        // Create the Extent test entry for this test
        ExtentTest t = null;
        try {
            Method overload = com.myvcf.reporting.ExtentReportManager.class
                    .getMethod("startTest", ITestResult.class, String.class);
            t = (ExtentTest) overload.invoke(null, result, displayName);
        } catch (NoSuchMethodException noOverload) {
            result.setAttribute("extent.displayName", displayName);
            t = com.myvcf.reporting.ExtentReportManager.startTest(result);
        } catch (Exception reflectionIssue) {
            t = com.myvcf.reporting.ExtentReportManager.startTest(result);
        }

        // Assign a category (tag) to the test for filtering in the report
        if (t != null) {

            if (scenarioFromTL != null && !scenarioFromTL.isBlank()) {
                t.assignCategory(normalizeLabel(scenarioFromTL));
            } else if (idLabel != null && !idLabel.isBlank()) {
                t.assignCategory(normalizeLabel(idLabel));
            } else {
                t.assignCategory(normalizeLabel(result.getMethod().getDescription()));
            }

            // Add a small info message indicating scenario parameters were loaded
            t.info("📑 Scenario parameters loaded");
        }
    }

    // Called when the test passes.
    // Marks the test as passed in the report.
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t != null) t.pass("🎯 Test passed");
        com.myvcf.reporting.ExtentReportManager.endTest();
    }

    // Called when the test is skipped.
    // Ensures the test still appears in the report with the correct status.
    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t == null) {
            String displayName = result.getMethod().getMethodName() + " [SKIPPED]";
            try {
                Method overload = com.myvcf.reporting.ExtentReportManager.class
                        .getMethod("startTest", ITestResult.class, String.class);
                t = (ExtentTest) overload.invoke(null, result, displayName);
            } catch (NoSuchMethodException noOverload) {
                result.setAttribute("extent.displayName", displayName);
                t = com.myvcf.reporting.ExtentReportManager.startTest(result);
            } catch (Exception ignore) {
                t = com.myvcf.reporting.ExtentReportManager.startTest(result);
            }
        }
        Throwable ex = result.getThrowable();
        if (ex != null) t.skip(ex); else t.skip("Test skipped");
        com.myvcf.reporting.ExtentReportManager.endTest();
    }

    // Called when a test fails.
    // Delegates failure handling to a helper method.
    @Override
    public void onTestFailure(ITestResult result) {
        handleFailure("FAIL", result);
    }

    // Called when a configuration method (like @BeforeMethod) fails.
    // Uses the same failure handling logic.
    @Override
    public void onConfigurationFailure(ITestResult result) {
        handleFailure("CONFIG_FAIL", result);
    }

    // Handles failures and attaches screenshots to the Extent report
    private void handleFailure(String tag, ITestResult result) {
        ExtentTest t = com.myvcf.reporting.ExtentReportManager.getTest();
        if (t == null) {
            String fallbackName = result.getMethod().getMethodName() + " [" + tag + "]";
            try {
                Method overload = com.myvcf.reporting.ExtentReportManager.class
                        .getMethod("startTest", ITestResult.class, String.class);
                t = (ExtentTest) overload.invoke(null, result, fallbackName);
            } catch (NoSuchMethodException noOverload) {
                result.setAttribute("extent.displayName", fallbackName);
                t = com.myvcf.reporting.ExtentReportManager.startTest(result);
            } catch (Exception ignore) {
                t = com.myvcf.reporting.ExtentReportManager.startTest(result);
            }
        }

        // Log the failure exception in the report
        Throwable ex = result.getThrowable();
        if (ex != null) t.log(Status.FAIL, ex);

        // Attempt to capture a screenshot using reflection
        Object instance = result.getInstance();
        File shot = null;
        try {
            Method m = findMethod(instance.getClass(), "captureScreenshot", String.class);
            if (m != null) {
                m.setAccessible(true);
                String hint = String.format("%s__%s__%s",
                        result.getTestClass().getRealClass().getSimpleName(),
                        result.getMethod().getMethodName(),
                        tag
                );
                shot = (File) m.invoke(instance, hint);
            }
        } catch (Exception e) {
            t.warning("Screenshot capture failed: " + e.getMessage());
        }

        // Convert screenshot to Base64 and embed directly into the report
        try {
            if (shot != null && shot.exists()) {
                byte[] bytes = Files.readAllBytes(shot.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                t.fail("Screenshot",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64, "Failure Screenshot").build());
            }
        } catch (Exception io) {
            t.warning("Could not embed failure screenshot: " + io.getMessage());
        }

        com.myvcf.reporting.ExtentReportManager.endTest();
    }

    // Utility method to search for a method in the class hierarchy
    private static Method findMethod(Class<?> cls, String name, Class<?>... types) {
        Class<?> c = cls;
        while (c != null) {
            try { return c.getDeclaredMethod(name, types); }
            catch (NoSuchMethodException ignore) { c = c.getSuperclass(); }
        }
        return null;
    }

   

    // Extracts the scenario name from test parameters
    private String extractScenario(Object row) {

        if (row instanceof String s) {
            return normalizeLabel(s);
        }

        try {
            Object sn = row.getClass().getMethod("getScenarioName").invoke(row);
            if (sn != null && !String.valueOf(sn).isBlank()) {
                return normalizeLabel(String.valueOf(sn));
            }
        } catch (ReflectiveOperationException ignore) {}

        try {
            Object s = row.getClass().getMethod("getScenario").invoke(row);
            return s == null ? "" : normalizeLabel(String.valueOf(s));
        } catch (ReflectiveOperationException ignore) {}

        if (row instanceof Map<?, ?> map) {
            Object v = map.get("scenario");
            return v == null ? "" : normalizeLabel(String.valueOf(v));
        }

        return "";
    }

    // Cleans labels so they are safe to use as report categories
    private String normalizeLabel(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        String cleaned = trimmed.replaceAll("[^\\p{L}\\p{N} _-]", "");
        return cleaned;
    }
}