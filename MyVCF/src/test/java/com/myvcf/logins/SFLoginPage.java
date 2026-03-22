package com.myvcf.logins;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;

import com.myvcf.pages.common.BasePage;

public class SFLoginPage extends BasePage {

	private final By loginButton = By.xpath("//input[@id='Login']");

	public SFLoginPage(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

	public void loginToSFInternal(String username, String password) {

		enterUsername(username);
		enterPassword(password);
		clickLoginButton();

	}

	private void enterUsername(String username) {

		clearAndTypeByLabel(username, "Username");

	}

	private void enterPassword(String password) {

		clearAndTypeByLabel(password, "Password");

	}

	private void clickLoginButton() {

		safeClick(loginButton);

	}

}
