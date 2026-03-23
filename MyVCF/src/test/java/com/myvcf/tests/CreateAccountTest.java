package com.myvcf.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.myvcf.core.ConfigReader;
import com.myvcf.core.JsonDataReader;
import com.myvcf.externalpages.CreateAccountsPage;
import com.myvcf.internalpages.SFHomePage;
import com.myvcf.internalpages.SFPersonAccountPage;
import com.myvcf.logins.LoginPage;
import com.myvcf.logins.SFLoginPage;
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

	private String normalizeValue(String value) {
		if (value == null) {
			return null;
		}
		return value.replaceAll("\\D", "");
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
		// Verify the success message
		Assert.assertTrue(createAccountsPage.successfullAccountCreationMessageIsDisplayed());

		// Navigate to MyVCF Internal

		openSalesforceInternal();
		String fullName = data.getFirstName() + " " + data.getMiddleName() + " " + data.getLastName();

		goToPersonAccountPage(fullName, data.getEmail());
		SFPersonAccountPage sfPersonAccountPage = new SFPersonAccountPage(driver);

		// Soft assertion: check value but don't stop execution if it fails

		// Validate that the Account Name displayed matches the expected full name
		softAssert.assertEquals(sfPersonAccountPage.getAccountName(), fullName, "fullName mismatch");

		// Validate that the Email displayed matches the expected email from test data
		softAssert.assertEquals(sfPersonAccountPage.getEmail(), data.getEmail(), "Email mismatch");

		// Validate expected Birth Date in MM/DD/YYYY format and validate it matches UI
		String birthDate = data.getMonth() + "/" + data.getDay() + "/" + data.getYear();
		softAssert.assertEquals(sfPersonAccountPage.getBirthDate(), birthDate, "birthDate mismatch");

		// Validate whether SSN presence flag (Yes/No) matches expected data
		softAssert.assertEquals(sfPersonAccountPage.getHasSSN(), data.getHasSsn(), "hasSSN mismatch");

		// Validate that the SSN value matches expected SSN
		softAssert.assertEquals(sfPersonAccountPage.getSSN(), normalizeValue(data.getSsn()), "SSN mismatch");

		// Validate whether ITIN presence flag (Yes/No) matches expected data
		softAssert.assertEquals(sfPersonAccountPage.getHasItin(), data.getHasItin(), "hasITIn mismatch");

		// Validate that the ITIN value matches expected ITIN

		softAssert.assertEquals(sfPersonAccountPage.getItin(), normalizeValue(data.getItin()), "ITIN mismatch");

		// Validate that the Alternate ID Type (e.g., Driver License, Passport) matches
		// expected value

		softAssert.assertEquals(sfPersonAccountPage.getAlternateIdType(), data.getIdType(),
				"Alternate ID Type mismatch");

		// Validate that the National ID Number matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getNationalIDNumber(), data.getNationalId(),
				"National ID Number mismatch");

		// Validate that the Other ID Number matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getOtherIDNumber(), data.getOtherIdNumber(),
				"Other ID Number mismatch");

		// Validate that the ID Issuer (issuing authority) matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getIdIssuer(), data.getIdIssuer(), " ID Issuer  mismatch");
		
		softAssert.assertAll();

	}

}
