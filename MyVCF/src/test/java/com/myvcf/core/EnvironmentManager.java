package com.myvcf.core;
import java.util.Locale;
/**
 * EnvironmentManager
 * I created a central class for managing environment specific configuration.
 * Supports easy retrieval of URLs, credentials, and environment metadata. 
 *   EnvironmentManager.getUrl("EP", "SIT");
 *   EnvironmentManager.getCurrentEnv();  
 */
public class EnvironmentManager {
    // Default values (can be overridden via -Denv or config.properties)
    private static final String DEFAULT_ENV = "QA";
    private static final String DEFAULT_PORTAL = "EP";
    /**
     * Returns the current active environment (QA, SIT, UAT, PreProd).
     */
    public static String getCurrentEnv() {
        String env = System.getProperty("env", ConfigReader.get("env", DEFAULT_ENV));
        return env.trim().toUpperCase(Locale.ROOT);
    }
    /**
     * Returns the current active portal type (EP or SF).
     */
    public static String getCurrentPortal() {
        String portal = System.getProperty("portal", ConfigReader.get("portal", DEFAULT_PORTAL));
        return portal.trim().toUpperCase(Locale.ROOT);
    }
    /**
     * Resolves a URL for the given portal and environments.
     */
    public static String getUrl(String portal, String env) {
        if (portal == null || env == null)
            throw new IllegalArgumentException("[EnvironmentManager] Portal or Environment cannot be null.");
        String key = portal.toUpperCase(Locale.ROOT) + "." + env.toUpperCase(Locale.ROOT);
        String url = ConfigReader.get(key);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("[EnvironmentManager] Missing URL for key: " + key);
        }
        return url.trim();
    }
    /**
     * Returns the currently active base URL based on config or system properties.
     */
    public static String getActiveBaseUrl() {
        return getUrl(getCurrentPortal(), getCurrentEnv());
    }
    /**
     * Summary log for reporting.
     */
    public static String summary() {
        return String.format("Active Environment → %s | Portal → %s | URL → %s",
                getCurrentEnv(), getCurrentPortal(), getActiveBaseUrl());
    }
}
