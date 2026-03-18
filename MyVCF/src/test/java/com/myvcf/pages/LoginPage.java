package com.myvcf.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.myvcf.pages.common.BasePage;

public class LoginPage extends BasePage {

	private final By createAccountButton = By.xpath("//span[contains(text(),'Create an account')]//parent::button");

	public void clickCreateAccountButton() {

		safeClick(createAccountButton);

	}

	public LoginPage(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

}
