package com.myvcf.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.myvcf.core.ConfigReader;
import com.myvcf.core.JsonDataReader;
import com.myvcf.pages.CreateAccountsPage;
import com.myvcf.pages.LoginPage;
import com.myvcf.pages.salesforceinternal.SFHomePage;
import com.myvcf.pages.salesforceinternal.SFLoginPage;
import com.myvcf.pages.salesforceinternal.SFPersonAccountPage;
import com.myvcf.pojo.CreateAccountData;

public class CreateAccountTest extends BaseTest {

	@DataProvider(name = "createAccountData")
	public Object[][] createAccountData() {
		List<CreateAccountData> rows = JsonDataReader.readResourceList("/data/createAccount.json",
				CreateAccountData[].class);
		return rows.stream().map(r -> new Object[] { r }).toArray(Object[][]::new);
	}

	private void gotoCreateAccountPage() {
		step("navigating to create account page");
		openUrl(resolvedBaseUrlFromConfig());
		LoginPage loginPage = new LoginPage(driver);
		loginPage.clickCreateAccountButton();

	}

	private void openSalesforceInternal() {

		openUrl(ConfigReader.getSalesforceUrl("SIT"));

		SFLoginPage sfLoginPage = new SFLoginPage(driver);
		sfLoginPage.loginToSFInternal(ConfigReader.getSalesforceAdminUsername("SIT"),
				ConfigReader.getSalesforceAdminPassword("SIT"));

	}

	private void goToPersonAccountPage(String fullName, String email) {

		SFHomePage sfHomePage = new SFHomePage(driver);
		sfHomePage.openAllAccount();
		sfHomePage.searchForAccount(email);
		sfHomePage.clickPersonAccountLink(fullName);

	}

	@Test(dataProvider = "createAccountData")
	public void createAccountTest(CreateAccountData data) {

		SoftAssert softAssert = new SoftAssert();

		resetStepCounter();
		step("Scenario:" + data.getScenario());
		gotoCreateAccountPage();
		CreateAccountsPage createAccountsPage = new CreateAccountsPage(driver);
		step("filling out create account form");
		createAccountsPage.filloutCreateAccountForm(data);
		//Verify the success message 
		Assert.assertTrue(createAccountsPage.successfullAccountCreationMessageIsDisplayed());
		
		// Navigate to MyVCF Internal 
		openSalesforceInternal();
		String fullName = data.getFirstName() + " " + data.getMiddleName() + " " + data.getLastName();
		goToPersonAccountPage(fullName, data.getEmail());
		SFPersonAccountPage sfPersonAccountPage = new SFPersonAccountPage(driver);
		//Validate Name, Email, DOB, SSN, HasSSN.
		//Soft assertion: check value but don't stop execution if it fails
		softAssert.assertEquals(sfPersonAccountPage.getAccountName(), fullName, "fullName mismatch");
		softAssert.assertEquals(sfPersonAccountPage.getEmail(), data.getEmail(), "Email mismatch");
		String birthDate = data.getMonth() + "/" + data.getDay() + "/" + data.getYear();
		softAssert.assertEquals(sfPersonAccountPage.getBirthDate(), birthDate, "birthDate mismatch");
		softAssert.assertEquals(sfPersonAccountPage.getHasSSN(), data.getHasSsn(), "hasSSN mismatch");
		softAssert.assertEquals(sfPersonAccountPage.getSSN(), data.getSsn().replaceAll("\\D+", ""), "SSN mismatch");
		softAssert.assertAll();

	}

}
