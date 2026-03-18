package com.myvcf.pages.salesforceinternal;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;

import com.myvcf.pages.common.BasePage;

public class SFHomePage extends BasePage {

	private final By dropDown = By.xpath("//button[@title = 'Select a List View: Accounts']");

	public void searchForAccount(String email) {

		clearAndTypeByLabel(email, "Search");

	}

	public void openAllAccount() {

		safeClick(dropDown);

		selectByVisibleTextNearLabel("Recent List Views", "All Accounts");
	}

	public void clickPersonAccountLink(String name) {
		safeClick(By.xpath("//a[contains(@title, '" + name + "')]"));
	}

	public SFHomePage(WebDriver driver) {
		super(driver);

	}

}
