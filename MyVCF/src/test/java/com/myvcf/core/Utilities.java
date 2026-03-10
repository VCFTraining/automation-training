package com.myvcf.core;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * ===================================================================
 *  The general-purpose helper methods for UI interactions, debugging,
 *  and to help with test data generation for myVCF. 
 * ===================================================================
 */
public final class Utilities {

    private Utilities() {
       
    }
    // =========================
    // 1. UI Interaction Helpers
    // =========================

    /**
     * Highlights an element on the screen for debugging visibility.
     * @param driver The active WebDriver instance.
     * @param element The element to highlight.
     * @param note Optional message to print to console.
     */
    public static void highlight(WebDriver driver, WebElement element, String note) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].setAttribute('style', 'outline:3px solid orange; outline-offset:2px;');",
                    element
            );
            if (note != null && !note.isBlank()) {
                System.out.println("[Highlight] " + note);
            }
        } catch (Exception e) {
            System.err.println("[Highlight] Failed: " + e.getMessage());
        }
    }

    /**
     * Safely attempts to click an element using multiple fallback methods.
     * Tries Actions → then JS → lastly, direct click.
     * @param driver The WebDriver instance.
     * @param element The element to click.
     */
    public static void safeClick(WebDriver driver, WebElement element) {
        for (int i = 0; i < 3; i++) {
            try {
                new Actions(driver).moveToElement(element).click().perform();
                return;
            } catch (Exception e1) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    return;
                } catch (Exception e2) {
                    System.out.println("[safeClick] Retry " + (i + 1) + " failed: " + e2.getMessage());
                }
            }
        }
        throw new RuntimeException("[safeClick] Unable to click element after retries.");
    }

    /**
     * Scrolls the element into the visible viewport.
     * @param driver The WebDriver instance.
     * @param element The element to scroll to.
     */
    public static void scrollIntoView(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (Exception e) {
            System.err.println("[ScrollIntoView] Failed: " + e.getMessage());
        }
    }

    // =========================
    // 2. Random Data Generators
    // =========================

    /**
     * Generates a unique random email address using a prefix and time stamp.
     * This to help with new implementation to handle the Unrep Email 
     * @param prefix A prefix like "qa_user_" or "test_"
     * @return Randomized email address.
     */
    public static String randomEmail(String prefix) {
        long ts = System.currentTimeMillis() % 1000000;
        String validPrefix = (prefix == null || prefix.isBlank())
                ? Constants.DEFAULT_USER_PREFIX
                : prefix;
        return validPrefix + ts + Constants.DEFAULT_EMAIL_DOMAIN;
    }

    /**
     * Generates a random numeric string of given length.
     *
     * @param length Desired number of digits.
     * @return A random numeric string.
     */
    public static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    // ========================
    // 3. String & Data Helpers
    // ========================

    /**
     * Removes all non digit characters from a string.
     * @param input Input string.
     * @return Digits-only string or empty if input is null.
     */
    public static String digitsOnly(String input) {
        return input == null ? "" : input.replaceAll("\\D+", "");
    }

    /**
     * Checks if a string is not null and not blank.
     * @param s Input string.
     * @return True if not blank.
     */
    public static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Normalizes a file path for OS compatibility.
     * @param path Input path string.
     * @return Normalized path using forward slashes.
     */
    public static String normalizePath(String path) {
        return path == null ? "" : path.replace("\\", "/");
    }

    // ========================
    // 4. Pause & Debug Helpers
    // ========================

    /**
     * Simple pause for debugging purposes.
     * @param ms Duration in milliseconds.
     * @param reason Optional reason to display.
     */
    public static void pause(long ms, String reason) {
        try {
            if (reason != null && !reason.isBlank()) {
                System.out.println("[Pause] " + reason + " (" + ms + " ms)");
            }
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
    // ========================
    // 5. Wait Helper method
    // ========================
    public static WebElement waitForElementVisible(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

 // =========================
    // WAIT FOR ELEMENT TO BE CLICKABLE
    // =========================
    public static WebElement waitForElementClickable(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Timeout of 10 seconds
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    
}
