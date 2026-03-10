package com.myvcf.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

// Optional: also capture @Before/@After failures
import org.testng.IConfigurationListener;

import java.io.File;
import java.lang.reflect.Method;

public class ScreenshotListener implements ITestListener, IConfigurationListener {

    // Called automatically by TestNG when a test fails.
    // This triggers screenshot capture for the failed test.
    @Override
    public void onTestFailure(ITestResult result) {
        takeFailureShot(result);
    }

    // Called when a configuration method fails
    // (e.g., @BeforeMethod, @BeforeClass, @AfterMethod).
    // Useful because sometimes tests fail before the test method even runs.
    @Override
    public void onConfigurationFailure(ITestResult result) {
        takeFailureShot(result);
    }

    // Handles the actual screenshot capture logic.
    // Uses reflection to call captureScreenshot(String) from the test class or BaseTest.
    private void takeFailureShot(ITestResult result) {
        Object instance = result.getInstance();

        // Build a descriptive screenshot file name.
        // Format example: PortalTitleTest__searchportal__FAIL
        String browser = System.getProperty("browser", "");
        String hint = String.format("%s__%s__FAIL",
                result.getTestClass().getRealClass().getSimpleName(), // e.g., PortalTitleTest
                result.getMethod().getMethodName(),                   // e.g., searchportal
                browser                                               // e.g., chrome
        );

        try {
            // Look for the captureScreenshot(String) method even if it is protected
            // and declared in a parent class (like BaseTest).
            Method m = findMethod(instance.getClass(), "captureScreenshot", String.class);

            if (m == null) {
                Reporter.log("[FAILURE SCREENSHOT] captureScreenshot(String) not found on test instance or its superclasses.", true);
                return;
            }

            m.setAccessible(true);

            // Call the screenshot method dynamically
            File f = (File) m.invoke(instance, hint);

            if (f != null) {
                // Log the screenshot path in TestNG report as a clickable link
                String href = "file:///" + f.getAbsolutePath().replace("\\", "/");
                Reporter.log("<a href='" + href + "'>[failure screenshot]</a>", true);
            } else {
                Reporter.log("[FAILURE SCREENSHOT] captureScreenshot returned null.", true);
            }

        } catch (Exception e) {
            Reporter.log("[FAILURE SCREENSHOT ERROR] " + e.getMessage(), true);
        }
    }

    // Utility method used to find a method in the class or any of its parent classes.
    // This helps when captureScreenshot() is implemented in BaseTest instead of the test class.
    private static Method findMethod(Class<?> cls, String name, Class<?>... types) {
        Class<?> c = cls;
        while (c != null) {
            try { return c.getDeclaredMethod(name, types); }
            catch (NoSuchMethodException ignored) { c = c.getSuperclass(); }
        }
        return null;
    }
}