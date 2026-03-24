package com.myvcf.internalpages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.myvcf.pages.common.BasePage;

public class SFPersonAccountPage extends BasePage {

	public String getFieldTextByLabel(String label) {
		String fieldXpath = "//span[contains(@class,'field-label') and normalize-space()='" + escape(label) + "']"
				+ "/ancestor::div[contains(@class,'slds-form-element')]";

		WebElement field = waitVisible(By.xpath(fieldXpath));

		List<WebElement> valueEls = field.findElements(By.xpath(
				".//span[contains(@class,'test-id__field-value') or contains(@class,'slds-form-element__static')]"));

		if (valueEls.isEmpty()) {
			return null; // or "" if you want blank string
		}

		WebElement el = valueEls.get(0);

		String text = el.getText();
		if (text != null && !text.trim().isEmpty()) {
			return text.trim();
		}

		Object jsText = ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", el);

		String result = jsText == null ? null : jsText.toString().trim();
		return (result == null || result.isEmpty()) ? null : result;
	}

	public String getAccountName() {

		return getFieldTextByLabel("Account Name");
	}

	public String getEmail() {

		return getFieldTextByLabel("Email");
	}

	public String getBirthDate() {

		return getFieldTextByLabel("Birthdate");
	}

	public String getSSN() {
		return getFieldTextByLabel("SSN");
	}

	public boolean getHasSSN() {

		return getFieldTextByLabel("Has SSN").equals("Yes");

	}

	public SFPersonAccountPage(WebDriver driver) {
		super(driver);

	}

	public Boolean getHasItin() {

		String hasITINValue = getFieldTextByLabel("Has ITIN");
		if (hasITINValue == null || hasITINValue.isBlank()) {
			System.out.println("Has ITIN value is blank.");
			return null;
		}
		System.out.println("Has ITIN value is " + hasITINValue);
		return hasITINValue.equals("Yes");
	}

	public String getItin() {
		return getFieldTextByLabel("ITIN");
	}

	public String getAlternateIdType() {
		return getFieldTextByLabel("Alternate ID Type");
	}

	public String getNationalIDNumber() {
		return getFieldTextByLabel("National ID");
	}

	public String getOtherIDNumber() {
		return getFieldTextByLabel("Other ID");
	}

	public String getIdIssuer() {
		return getFieldTextByLabel("ID Issuer");
	}

}
