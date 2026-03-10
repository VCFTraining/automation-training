package com.myvcf.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    // ThreadLocal keeps a separate WebDriver instance per thread.
    // This is important when running tests in parallel so drivers don't interfere with each other.
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    // Returns the current thread's WebDriver instance.
    // Tests and utilities call this instead of storing drivers themselves.
    public static WebDriver getDriver() {
        return driver.get();
    }

    // Safely quits the current WebDriver and removes it from ThreadLocal.
    // This prevents memory leaks and ensures the browser fully closes after a test run.
    public static void quitDriver() {
        WebDriver d = driver.get();
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) {}
            driver.remove();
        }
    }

    // Creates a new WebDriver based on the requested browser and headless flag.
    // This is the main entry point used by the test setup to start a browser session.
    public static void create(String browser, boolean headless) {
        WebDriver d;

        // Decide which browser to start based on the provided name.
        switch (browser.toLowerCase()) {

            // Setup and start Chrome browser
            case "chrome": {
                WebDriverManager.chromedriver().setup(); // .driverVersion("140.0.7339.207")
                ChromeOptions opts = new ChromeOptions();

                // If headless mode is enabled, run the browser without UI
                if (headless) {
                    opts.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                } else {
                    opts.addArguments("--start-maximized");
                }

                // Add only if you truly need it:
                // opts.addArguments("--remote-allow-origins=*");

                d = new ChromeDriver(opts);
                break;
            }

            // Setup and start Firefox browser
            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();

                // Run Firefox in headless mode if requested
                if (headless) {
                    opts.addArguments("-headless");
                }

                d = new FirefoxDriver(opts);
                break;
            }

            // Setup and start Edge browser
            case "edge": {

                // Path to EdgeDriver executable.
                // If not passed via system property, the default local path is used.
                String edgePath = System.getProperty(
                        "edge.driver",
                        "C:\\Users\\vcfadm-mzuhairi\\Driver\\msedgedriver.exe"
                );

                System.setProperty("webdriver.edge.driver", edgePath);

                EdgeOptions opts = new EdgeOptions();

                // Configure headless or normal browser startup
                if (headless) {
                    opts.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                } else {
                    opts.addArguments("--start-maximized");
                }

                d = new EdgeDriver(opts);
                break;
            }

            // Throw an error if someone passes a browser the framework does not support
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        // Configure WebDriver timeouts.
        // These values can be overridden through JVM system properties if needed.
        d.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(Long.getLong("timeout.pageLoad", 30)))
                .scriptTimeout(Duration.ofSeconds(Long.getLong("timeout.script", 30)))
                .implicitlyWait(Duration.ofSeconds(Long.getLong("timeout.implicit", 0)));

        // Store the driver in ThreadLocal so each test thread gets its own instance.
        driver.set(d);
    }
}