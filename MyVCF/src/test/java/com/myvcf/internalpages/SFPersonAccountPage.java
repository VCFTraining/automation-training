package com.myvcf.internalpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.myvcf.pages.common.BasePage;

public class SFPersonAccountPage extends BasePage {

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
