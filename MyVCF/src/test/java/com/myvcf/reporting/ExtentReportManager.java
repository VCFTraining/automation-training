
package com.myvcf.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExtentReportManager {

    // Main Extent report object shared across the test run.
    private static ExtentReports extent;

    // Keeps the current test report object per thread.
    // This helps when tests run in parallel.
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    // Private constructor so this utility class does not get instantiated.
    private ExtentReportManager() {}

    // ---- expose the final report path so the listener can print/link it
    // Stores the final generated report path so listeners can print or link it later.
    private static String REPORT_PATH;

    // Returns the final report file path.
    public static String getReportPath() { return REPORT_PATH; }

    // Creates the Extent report only once and returns it.
    // This is the main setup method for the HTML report.
    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            try {
                String base = System.getProperty(
                        "report.dir",
                        System.getProperty("user.dir") + File.separator + "target" + File.separator + "extent"
                );

                // Make sure the report folder exists before creating the file
                Files.createDirectories(new File(base).toPath());

                // Build a timestamped report file name so each run gets its own report
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String reportPath = base + File.separator + "ExtentReport_" + ts + ".html";
                REPORT_PATH = reportPath; // store the resolved report path

                // Configure the Extent Spark HTML reporter
                ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
                spark.config().setDocumentTitle("myVCF Automation Report");
                spark.config().setReportName("myVCF UI Tests");
                spark.config().setTheme(Theme.STANDARD); // or Theme.DARK

                // Create the main ExtentReports object and attach the reporter
                extent = new ExtentReports();
                extent.attachReporter(spark);

                // Add a few system details to the report
                extent.setSystemInfo("OS", System.getProperty("os.name"));
                extent.setSystemInfo("Java", System.getProperty("java.version"));
                extent.setSystemInfo("User", System.getProperty("user.name"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize ExtentReports", e);
            }
        }
        return extent;
    }

    /**
     * Starts an Extent test. If the ITestResult has an attribute "extent.displayName",
     * that value is used as the test's display name (set by your listener).
     * Otherwise, it falls back to "ClassName.methodName".
     */
    // Creates a new test entry in the Extent report for the current TestNG test.
    public static ExtentTest startTest(ITestResult result) {

        // Try to use a custom display name set earlier by the listener
        Object attr = result.getAttribute("extent.displayName");
        String displayName = (attr instanceof String) ? ((String) attr).trim() : "";

        // If no custom name exists, build a default one using class + method name
        if (displayName.isEmpty()) {
            String method = result.getMethod().getMethodName();
            String cls = result.getTestClass().getRealClass().getSimpleName();
            displayName = cls + "." + method;
        }

        ExtentTest test = getExtent()
                .createTest(displayName)
                // Keep TestNG groups as categories in the report
                .assignCategory(result.getMethod().getGroups());

        // Save the current test in ThreadLocal so each thread tracks its own report entry
        CURRENT.set(test);
        return test;
    }

    // Returns the current thread's active Extent test.
    public static ExtentTest getTest() { return CURRENT.get(); }

    // Clears the current thread's Extent test after the test finishes.
    public static void endTest() { CURRENT.remove(); }

    // Writes everything to the report file at the end of execution.
    public static synchronized void flush() {
        if (extent != null) extent.flush();
    }
}