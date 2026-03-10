package com.myvcf.core;

/**
 * ============================================================
 *  This Global constants for the myVCF Selenium Automation Framework.
 *  Store & shared static values to avoid hard coding across the project.
 * ============================================================
 */
public final class Constants {

    // Private constructor so nobody accidentally creates an instance of this class.
    // This class is only meant to hold shared constants.
    private Constants() {
    
    }

    //  Timeout Values (seconds) 
    // Common wait times used across tests and utilities.
    // Keeps all wait values in one place so we don't hardcode them everywhere.
    public static final long SHORT_WAIT = 3;
    public static final long MEDIUM_WAIT = 10;
    public static final long LONG_WAIT = 20;
    public static final long DEFAULT_EXPLICIT_WAIT = 10;

    //  File & Directory Paths 
    // Standard locations used by the framework for saving artifacts and reading test data.
    public static final String SCREENSHOT_DIR = "target/screenshots/";
    public static final String EXTENT_REPORT_DIR = "target/extent/";
    public static final String JSON_DATA_PATH = "src/test/resources/data/";

    //  Config Property Keys 
    // Keys used when reading values from config.properties or environment variables.
    public static final String BASE_URL_KEY = "baseUrl";
    public static final String BROWSER_KEY = "browser";
    public static final String ENV_KEY = "env";

    //  Date & Time Formats 
    // Common date formats used for logs, file names, and reporting.
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd_HH.mm.ss";

    //  Portal Identifiers 
    // Identifiers used when selecting which portal/environment the test should run against.
    public static final String PORTAL_EXTERNAL = "EP.UAT";
    public static final String PORTAL_SF = "SF.UAT";

    //  Miscellaneous 
    // General default values used across the framework when generating test data.
    public static final String DEFAULT_EMAIL_DOMAIN = "@testmail.com";
    public static final String DEFAULT_USER_PREFIX = "automationuser@usdoj.gov.vcf.uat";
}