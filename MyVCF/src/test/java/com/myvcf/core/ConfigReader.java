package com.myvcf.core;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigReader {
    private static final Properties PROPS = new Properties();
    private static final int MAX_INTERPOLATION_DEPTH = 10;
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    static {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            } else {
                System.err.println("[ConfigReader] config.properties not found on classpath.");
            }
        } catch (Exception e) {
            System.err.println("[ConfigReader] Failed to load config.properties: " + e.getMessage());
        }
    }

    // Private constructor so nobody creates an object from this utility class.
    private ConfigReader() {}

    // Converts the config key to env variable format.
    // Example: base.url becomes BASE_URL.
    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    /** Standard precedence: System property -> Env var -> config.properties -> default */
    // Main getter for reading config values.
    // We check system props first, then env vars, then config file, and use default if nothing is found.
    public static String get(String key, String def) {
        String v = System.getProperty(key);
        if (v != null && !v.isBlank()) return v;

        v = System.getenv(toEnvKey(key));
        if (v != null && !v.isBlank()) return v;

        v = PROPS.getProperty(key);
        if (v != null && !v.isBlank()) return v;

        return def;
    }

    // Shortcut version of get when we don't want to pass a default value.
    public static String get(String key) {
        return get(key, null);
    }

    // Reads the value as int.
    // If parsing fails, we just return the default value instead of breaking the test.
    public static int getInt(String key, int def) {
        try { return Integer.parseInt(get(key, String.valueOf(def))); }
        catch (NumberFormatException ex) { return def; }
    }

    // Same idea as getInt, but for long values.
    public static long getLong(String key, long def) {
        try { return Long.parseLong(get(key, String.valueOf(def))); }
        catch (NumberFormatException ex) { return def; }
    }

    // Reads the value as boolean.
    // If the value is missing, it falls back to the default we pass in.
    public static boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(get(key, String.valueOf(def)));
    }

    /** Resolve with interpolation + alias resolution */
    // Gets the final resolved value.
    // This handles placeholders like ${...} and also supports alias lookup and baseUrl fallback logic.
    public static String getResolved(String key, String def) {
        String raw = get(key, null);
        if (raw == null || raw.isBlank()) {
            if ("baseUrl".equals(key)) {
                String env = get("environment", null);
                if (env != null && !env.isBlank()) {
                    String envUrl = get(env, null);
                    if (envUrl != null && !envUrl.isBlank()) return envUrl;
                }
            }
            return def;
        }

        String interpolated = interpolate(raw);
        String alias = get(interpolated, null);
        if (alias != null && !alias.isBlank()) {
            return alias;
        }
        return interpolated;
    }

    // Replaces placeholders inside the value.
    // Example: if a property has ${host}, this method swaps it with the real value.
    private static String interpolate(String input) {
        String result = input;
        for (int i = 0; i < MAX_INTERPOLATION_DEPTH; i++) {
            Matcher m = PLACEHOLDER.matcher(result);
            StringBuffer sb = new StringBuffer();
            boolean changed = false;
            while (m.find()) {
                String refKey = m.group(1).trim();
                String refVal = get(refKey, "");
                m.appendReplacement(sb, Matcher.quoteReplacement(refVal));
                changed = true;
            }
            m.appendTail(sb);
            result = sb.toString();
            if (!changed) break;
        }
        return result;
    }

    /** Convenience for baseUrl with smart fallback. */
    // Helper method just for baseUrl so callers don't need to know the internal fallback logic.
    public static String getBaseUrl(String def) {
        return getResolved("baseUrl", def);
    }

    // ===========================================================
    // Salesforce Environment-Specific Helpers
    // ===========================================================

    // Returns the Salesforce URL for the given environment.
    public static String getSalesforceUrl(String env) {
        return get("SF." + env, null);
    }

    // Returns the Salesforce username for the given environment.
    public static String getSalesforceUsername(String env) {
        return get("SF." + env + ".username", null);
    }

    // Returns the Salesforce password for the given environment.
    public static String getSalesforcePassword(String env) {
        return get("SF." + env + ".password", null);
    }
    
    
    
    
 // Returns the Salesforce AdminUsername for the given environment.
    public static String getSalesforceAdminUsername(String env) {
        return get("SF." + env + ".usernameAdmin", null);
    }

    // Returns the Salesforce AdminPassword for the given environment.
    public static String getSalesforceAdminPassword(String env) {
        return get("SF." + env + ".passwordAdmin", null);
    }
    
    
    
    
    

    /** Returns active environment (default = SIT). */
    // Figures out which environment is active right now.
    // If nothing is set, it defaults to SIT.
    public static String getActiveEnv() {
        return System.getProperty("env", get("env", "SIT")).toUpperCase();
    }
}