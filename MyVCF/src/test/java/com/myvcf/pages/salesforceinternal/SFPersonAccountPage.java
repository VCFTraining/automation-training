package com.myvcf.pages.salesforceinternal;

import org.openqa.selenium.WebDriver;

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

}
