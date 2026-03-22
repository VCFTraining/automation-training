package com.myvcf.internalpages;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;

import com.myvcf.pages.common.BasePage;

public class SFHomePage extends BasePage {

	private final By dropDown = By.xpath("//button[@title = 'Select a List View: Accounts']");

	public void searchForAccount(String email) {
		driver.navigate().refresh();
		waitVisible(By.xpath("//table[contains(@class, 'slds-table')]"));
		clearAndTypeByLabel(email, "Search");
		System.out.println("Refreshing the page to pull up the records");
		driver.navigate().refresh();
	}

	public void openAllAccount() {
		waitForDomReady();
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
