package com.myvcf.pages.common;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.Reporter;

import com.myvcf.tests.BaseTest;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public abstract class BasePage {

    // Shared WebDriver instance used by all page classes.
    protected final WebDriver driver;

    // Default explicit wait for this page.
    protected final WebDriverWait wait;

    // Fixed locale so string matching stays consistent.
    protected static final Locale LOC = Locale.ENGLISH;

 // Demo pacing (override via -Ddemo.pause.ms=2000 -Ddemo.key.ms=50)
    // Small pause values used when running demo mode or slower visual execution.
    private final long pauseMs  = Math.max(0L, Long.getLong("demo.pause.ms", 0));
    private final long keyDelay = Math.max(0L, Long.getLong("demo.key.ms", 0));
    
    // Default constructor for page objects.
    // Uses the framework default wait time unless another timeout is passed in.
    protected BasePage(WebDriver driver) {
        this(driver, Duration.ofSeconds(Long.getLong("wait.defaultSeconds", 10)));
    }

    // Main page constructor.
    // Stores the driver and creates the page-level explicit wait.
    protected BasePage(WebDriver driver, Duration timeout) {
        if (driver == null) throw new IllegalArgumentException("driver is null");
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    /* ============================================================
       Wait helpers 
       ============================================================ */

    // Returns the explicit wait object for reuse in page methods.
    protected WebDriverWait explicitWait() { return wait; }

    // Waits until the element is visible on the page.
    protected WebElement waitVisible(By locator) {
        return explicitWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Waits until the element is clickable.
    protected WebElement waitClickable(By locator) {
        return explicitWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Waits until the browser says the page DOM is fully loaded.
    protected void waitForDomReady() {
        explicitWait().until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    

    // --------- small utils ----------
    // Returns empty string instead of null.
    protected static String nz(String s) { return s == null ? "" : s; }

    // Quick check to make sure a string has real content.
    protected static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    // Keeps digits only and strips out anything else.
    protected static String digitsOnly(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }

    // Escapes single quotes so the value is safer to use inside XPath or JS strings.
    protected static String escape(String s) { return s == null ? "" : s.replace("'", "\\'"); }

    // --------- safeClick(By / WebElement) ----------
    // Tries multiple click strategies for a locator.
    // This helps when normal Selenium click is flaky because of overlays, stale elements, or scrolling issues.
    protected void safeClick(By locator) {
        int attempts = 6;
        RuntimeException last = null;

        for (int i = 1; i <= attempts; i++) {
            try {
                WebElement el = explicitWait().until(ExpectedConditions.presenceOfElementLocated(locator));
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); } catch (Exception ignored) {}

                try {
                    explicitWait().until(ExpectedConditions.elementToBeClickable(el)).click();
                    return;
                } catch (ElementClickInterceptedException ignored) {}

                try { ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -120);"); } catch (Exception ignored) {}

                try {
                    new Actions(driver).moveToElement(el, 1, 1).pause(Duration.ofMillis(80)).click().perform();
                    return;
                } catch (Exception ignored) {}

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                return;

            } catch (StaleElementReferenceException | ElementClickInterceptedException | MoveTargetOutOfBoundsException e) {
                last = new RuntimeException("safeClick(By) attempt " + i + " failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            } catch (TimeoutException e) {
                last = new RuntimeException("safeClick(By) attempt " + i + " timed out: " + locator, e);
            }
        }
        if (last != null) throw last;
    }

    // Same safe click idea, but this version works with an already found WebElement.
    protected void safeClick(WebElement el) {
        int attempts = 5;

        for (int i = 1; i <= attempts; i++) {
            try {
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); } catch (Exception ignored) {}

                try {
                    explicitWait().until(ExpectedConditions.elementToBeClickable(el)).click();
                    return;
                } catch (ElementClickInterceptedException ignored) {}

                try { ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-120);"); } catch (Exception ignored) {}

                try {
                    new Actions(driver).moveToElement(el, 1, 1).pause(Duration.ofMillis(80)).click().perform();
                    return;
                } catch (Exception ignored) {}

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                return;

            } catch (StaleElementReferenceException | ElementClickInterceptedException | MoveTargetOutOfBoundsException e) {
                try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("safeClick(WebElement) could not click after retries.");
    }

    // --------- inputByLabelSmart + helpers ----------
    // Finds an input field by trying one or more label names.
    // Good for forms where the locator can vary a bit between screens.
    protected WebElement inputByLabelSmart(String... labels) {
        return explicitWait().until(d -> tryFindInputByLabels(labels));
    }

    // Tries different locator patterns to find an input that belongs to the given label.
    protected WebElement tryFindInputByLabels(String... labels) {
        for (String label : labels) {
            if (label == null || label.isBlank()) continue;
            String lower = label.toLowerCase(LOC);

            for (By by : List.of(
                    By.xpath("//input[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"+escape(lower)+"')][not(@type='radio')][not(@type='checkbox')]"),
                    By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"+escape(lower)+"')][not(@type='radio')][not(@type='checkbox')]")
            )) {
                for (WebElement c : driver.findElements(by)) if (c.isDisplayed()) return c;
            }

            List<WebElement> grouped = driver.findElements(By.xpath(
                    "//*[contains(@class,'slds-form-element')]"
                            + "//*[self::label or contains(@class,'slds-form-element__label')]"
                            + "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"+escape(lower)+"')]"
                            + "/ancestor::*[contains(@class,'slds-form-element')][1]//input[not(@type='hidden')][not(@type='radio')][not(@type='checkbox')]"
            ));
            for (WebElement c : grouped) if (c.isDisplayed()) return c;

            List<WebElement> next = driver.findElements(By.xpath(
                    "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"+escape(lower)+"')]"
                            + "/following::input[1][not(@type='hidden')][not(@type='radio')][not(@type='checkbox')]"
            ));
            for (WebElement c : next) if (c.isDisplayed()) return c;

            WebElement shortcut = attributeShortcut(lower);
            if (shortcut != null) return shortcut;
        }
        throw new NoSuchElementException("Input not found for labels: " + String.join(" | ", labels));
    }

    // Shortcut locators for common fields we use a lot.
    // This gives us a fallback when label-based lookup is not enough.
    protected WebElement attributeShortcut(String lowerLabel) {
        String css = switch (lowerLabel) {
            case "first name" -> "input[name='firstName'], input[id*='first' i]";
            case "last name"  -> "input[name='lastName'],  input[id*='last' i]";
            case "email"      -> "input[type='email'], input[name='email'], input[id*='email' i]";
            case "date of birth", "confirm date of birth" ->
                    "input[aria-label*='Date of birth' i], input[name*='dob' i], input[id*='dob' i]";
            case "ssn", "social security number (ssn)" ->
                    "input[name*='ssn' i]:not([type='radio']), input[id*='ssn' i]:not([type='radio'])";
            case "confirm ssn", "confirm social security number" ->
                    "input[name*='confirm' i][name*='ssn' i]:not([type='radio']), input[id*='confirm' i][id*='ssn' i]:not([type='radio'])";
            default -> null;
        };
        if (css == null) return null;
        for (WebElement c : driver.findElements(By.cssSelector(css))) if (c.isDisplayed()) return c;
        return null;
    }

    // --------- typing ----------
    // Finds an input by label and types into it.
    // Retries a few times in case the page is still moving or rerendering.
    protected void clearAndTypeByLabel(String text, String... labels) {
        RuntimeException last = null;
        for (int i = 0; i < 3; i++) {
            try {
                WebElement el = inputByLabelSmart(labels);
                clearAndType(el, text);
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                last = new RuntimeException("clearAndTypeByLabel attempt " + (i+1) + " failed: " + e, e);
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }
        if (last != null) throw last;
    }

    // Clears the field safely, types the new value, then tabs out.
    protected void clearAndType(WebElement el, String text) {
        try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); } catch (Exception ignored) {}
        try { explicitWait().until(ExpectedConditions.elementToBeClickable(el)); } catch (Exception ignored) {}
        try { el.click(); } catch (ElementClickInterceptedException e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el); }
        try { el.clear(); } catch (Exception ignored) {}
        try { el.sendKeys(Keys.chord(Keys.CONTROL, "a")); el.sendKeys(Keys.DELETE); } catch (Exception ignored) {}

        if (text != null && !text.isEmpty()) el.sendKeys(text);
        try { el.sendKeys(Keys.TAB); } catch (Exception ignored) {}
    }
    
    /** Select a value from a <select> near a label; fallback to SLDS combobox. */
    // Selects a value from a dropdown near the given label.
    // Works for both native select elements and Salesforce SLDS comboboxes.
    protected void selectByVisibleTextNearLabel(String label, String value) {
        WebElement container = waitVisible(By.xpath(
                "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
              + escape(label.toLowerCase(LOC)) + "')]"
              + "/ancestor::*[contains(@class,'slds-form-element') or contains(@class,'form-group')][1]"
        ));
        // Native <select>
        for (WebElement sel : container.findElements(By.tagName("select"))) {
            if (sel.isDisplayed()) {
                highlight(sel, label + " <select>");
                new Select(sel).selectByVisibleText(value);
                return;
            }
        }
        // SLDS combobox
        List<WebElement> triggers = container.findElements(By.xpath(
                ".//*[@role='combobox'] | .//input[contains(@class,'slds-combobox__input')]"
              + " | .//button[contains(@class,'slds-combobox__input') or contains(@class,'slds-combobox__input-value')]"
        ));
        for (WebElement t : triggers) if (t.isDisplayed()) {
            highlight(t, label + " combobox");
            safeClick(t);
            try { t.sendKeys(Keys.chord(Keys.CONTROL, "a")); t.sendKeys(Keys.DELETE); t.sendKeys(value); } catch (Exception ignored) {}
            WebElement opt = waitVisible(By.xpath(
                    "//div[@role='listbox']//*[@role='option' and (normalize-space()='"+escape(value)+"' or .//span[normalize-space()='"+escape(value)+"'])]"
            ));
            highlight(opt, "Option: " + value);
            safeClick(opt);
            return;
        }
        throw new NoSuchElementException("Control near '"+label+"' not found (select/combobox).");
    }

    // Highlights the element in the browser so it is easier to see during debug/demo runs.
    private void highlight(WebElement el, String note) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const e=arguments[0];e.setAttribute('data-prev-style',e.getAttribute('style')||'');" +
                    "e.setAttribute('style',(e.getAttribute('style')||'')+';outline:3px solid orange; outline-offset:2px;');", el);
        } catch (Exception ignored) {}
        logDetail("HIGHLIGHT → " + note);
    }

    // Small helper for detailed logs in TestNG report.
    private void logDetail(String fmt, Object... args) {
        String msg = String.format("[DETAIL] " + fmt, args);
        Reporter.log(msg, true);
//        System.out.println(msg);
    }
    
    /** Robust radio clicker for SLDS/LWC radios. */
    // Clicks a radio option by first finding the group, then the matching option inside it.
    // This is made for UI where standard radio locators can be flaky.
    protected void clickRadio(String groupLabel, String optionText) {
        String g = groupLabel == null ? "" : groupLabel.toLowerCase(Locale.ROOT).trim();
        String o = optionText == null ? "" : optionText.toLowerCase(Locale.ROOT).trim();

        // Find the group container first
        List<By> groupLocators = List.of(
            By.xpath("//*[self::legend or self::label or contains(@class,'slds-form-element__label')]" +
                     "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(g) + "')]" +
                     "/ancestor::*[contains(@class,'slds-form-element') or @role='group' or @role='radiogroup'][1]"),
            By.xpath("//*[contains(@class,'slds-form-element') or @role='group' or @role='radiogroup']" +
                     "[.//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(g) + "')]][1]")
        );

        WebElement container = null;
        for (By by : groupLocators) {
            List<WebElement> found = driver.findElements(by);
            for (WebElement c : found) if (c.isDisplayed()) { container = c; break; }
            if (container != null) break;
        }

        if (container != null && tryClickOptionInside(container, o)) return;
        if (tryClickOptionInside(driver.findElement(By.tagName("body")), o)) return;

        // Fallback: raw radio input by value
        for (By by : List.of(
                By.xpath("//input[@type='radio' and translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='" + escape(o) + "']"),
                By.xpath("//input[@type='radio' and @value and contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + escape(o) + "')]")
        )) {
            List<WebElement> radios = driver.findElements(by);
            for (WebElement r : radios) {
                if (r.isDisplayed()) { highlight(r, "Radio input (value=" + optionText + ")"); safeClick(r); return; }
            }
        }
        throw new NoSuchElementException("Radio not found: [" + groupLabel + "] -> [" + optionText + "]");
    }

    // Tries different ways to find and click a radio option inside the given scope.
    private boolean tryClickOptionInside(WebElement scope, String lowerOption) {
        // 1) <label> containing text
        List<WebElement> labels = scope.findElements(By.xpath(
            ".//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(lowerOption) + "')]"
        ));
        for (WebElement lab : labels) if (lab.isDisplayed()) { highlight(lab, "Radio label: " + lab.getText()); safeClick(lab); return true; }

        // 2) label with span
        List<WebElement> spanLabels = scope.findElements(By.xpath(
            ".//label[.//span[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(lowerOption) + "')]]"
        ));
        for (WebElement lab : spanLabels) if (lab.isDisplayed()) { highlight(lab, "Radio label(span): " + lab.getText()); safeClick(lab); return true; }

        // 3) role=radio
        List<WebElement> roleRadios = scope.findElements(By.xpath(
            ".//*[@role='radio'][contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(lowerOption) + "') "
          + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(lowerOption) + "')]"
        ));
        for (WebElement r : roleRadios) if (r.isDisplayed()) { highlight(r, "Role=radio option"); safeClick(r); return true; }

        // 4) nearest input around a text node
        List<WebElement> texts = scope.findElements(By.xpath(
            ".//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + escape(lowerOption) + "')]"
        ));
        for (WebElement t : texts) {
            List<WebElement> radios = t.findElements(By.xpath(".//preceding::input[@type='radio'][1] | .//following::input[@type='radio'][1]"));
            for (WebElement r : radios) if (r.isDisplayed()) { highlight(r, "Radio input near text"); safeClick(r); return true; }
        }
        return false;
    }

    /** Type digits into masked/segmented SLDS/LWC inputs found by label.
     *  - Focuses the visible input.
     *  - Distributes digits across sibling segments when present (maxlength).
     *  - Types real key events via Actions.
     *  - Falls back to JS set + input/change events if the mask did not stick.
     */
    // Enters digits into masked or split inputs like SSN, DOB, or similar fields.
    // Handles cases where the value is split across multiple small inputs.
    protected void enterMaskedDigitsByLabel(String digits, String... labels) {
        String d = digitsOnly(digits);
        if (d.isEmpty()) return;

        // First input by label (what you already rely on everywhere)
        WebElement first = inputByLabelSmart(labels);
        highlight(first, labels[0] + " (masked)");

        // Try to collect all visible inputs within the same form element (handles split masks like 3-2-4)
        List<WebElement> inputs = first.findElements(By.xpath(
                "./ancestor::*[contains(@class,'slds-form-element')][1]" +
                "//input[not(@type='hidden') and not(@type='radio') and not(@type='checkbox')]"));
        inputs.removeIf(el -> !el.isDisplayed());

        if (inputs.isEmpty()) inputs = List.of(first); // single field case

        int idx = 0;
        for (WebElement in : inputs) {
            if (idx >= d.length()) break;

            int max = 0;
            try {
                String ml = in.getAttribute("maxlength");
                if (ml != null && !ml.isBlank()) max = Integer.parseInt(ml.trim());
            } catch (Exception ignored) { }
            if (max <= 0) max = d.length() - idx; // no maxlength → dump the rest

            String chunk = d.substring(idx, Math.min(d.length(), idx + max));
            focusAndTypeRealKeys(in, chunk);
            idx += chunk.length();
        }

        // If mask still didn't stick, force value + events on the first control
        if (digitsOnly(getValue(first)).length() < d.length()) {
            jsSetWithEvents(first, d);
        }
    }
    
    /** Focuses the element and types using real key events (Actions). */
    // Types into the field using Actions so the UI sees real keyboard events.
    // This helps with masks and client-side validators.
    private void focusAndTypeRealKeys(WebElement el, String text) {
        try {
            new Actions(driver)
                .moveToElement(el).click()
                .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .sendKeys(Keys.DELETE)
                .pause(Duration.ofMillis(Math.max(20, (int)keyDelay)))
                .sendKeys(text)
                .pause(Duration.ofMillis(Math.max(20, (int)keyDelay)))
                .sendKeys(Keys.TAB)
                .build().perform();
        } catch (Exception e) {
            // Fallback to direct typing if Actions fails
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el); } catch (Exception ignored) {}
            try { el.click(); } catch (Exception ignored) {}
            try { el.sendKeys(Keys.chord(Keys.CONTROL, "a")); el.sendKeys(Keys.DELETE); } catch (Exception ignored) {}
            el.sendKeys(text);
            try { el.sendKeys(Keys.TAB); } catch (Exception ignored) {}
        }
    }

    /** JS set + dispatch 'input'/'change' so the mask/validators see the new value. */
    // Last-resort fallback that sets the value with JavaScript and fires input/change events.
    private void jsSetWithEvents(WebElement el, String digits) {
        ((JavascriptExecutor) driver).executeScript(
            "const e=arguments[0], v=arguments[1];" +
            "e.value=v;" +
            "e.dispatchEvent(new Event('input',{bubbles:true}));" +
            "e.dispatchEvent(new Event('change',{bubbles:true}));",
            el, digits);
    }
    
    // Verifies that the masked field kept the expected digits after typing and blur.
    protected void assertDigitsStuck(WebElement el, String wantDigits, int expectedLen, String fieldName) {
        String gotDigits = digitsOnly(getValue(el));
        logDetail("%s verify | want=%s got=%s", fieldName, wantDigits, gotDigits);
        Assert.assertEquals(gotDigits.length(), expectedLen, fieldName + " masked length mismatch");
        Assert.assertEquals(gotDigits, wantDigits, fieldName + " value did not stick/match after blur");
    }
    
    // Reads the current value from the input using JavaScript.
    protected String getValue(WebElement el) {
        try {
            Object v = ((JavascriptExecutor) driver).executeScript("return arguments[0].value;", el);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception e) {
            return "";
        }
    }
    
}