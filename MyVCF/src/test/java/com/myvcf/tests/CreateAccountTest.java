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
		openUrl(resolvedBaseUrlFromConfig());
		// Step 1

		passStep("Opened MyVCF Portal App:" + ConfigReader.getActiveEnv());

		LoginPage loginPage = new LoginPage(driver);
		loginPage.clickCreateAccountButton();
		// Step 2
		passStep("Opened Create Account Page");

	}

	private void openSalesforceInternal() {

		openUrl(ConfigReader.getSalesforceUrl("SIT"));

		SFLoginPage sfLoginPage = new SFLoginPage(driver);
		sfLoginPage.loginToSFInternal(ConfigReader.getSalesforceAdminUsername("SIT"),
				ConfigReader.getSalesforceAdminPassword("SIT"));
		// Step 5
		passStep("Opened MyVCF Internal App" + ConfigReader.getActiveEnv());

	}

	private void goToPersonAccountPage(String fullName, String email) {

		SFHomePage sfHomePage = new SFHomePage(driver);
		sfHomePage.openAllAccount();
		// Step 6
		passStep("Opened All Account from Accounts dropdown in HomePage");
		sfHomePage.searchForAccount(email);
		sfHomePage.clickPersonAccountLink(fullName);

		// Step 7
		passStep("Opened Person Account of : " + fullName);

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

		gotoCreateAccountPage();

		CreateAccountsPage createAccountsPage = new CreateAccountsPage(driver);
		// Step 3
		passStep("Filling out create account form");

		createAccountsPage.filloutCreateAccountForm(data);
		// Verify the success message

		Assert.assertTrue(createAccountsPage.successfullAccountCreationMessageIsDisplayed());
		// Step 4
		passStep("Account Created Successfully");
		attachScreenshotToExtent("Success Message Verfied", "Success Message Verfied");

		// Navigate to MyVCF Internal

		openSalesforceInternal();

		String fullName = data.getFirstName() + " " + data.getMiddleName() + " " + data.getLastName();

		goToPersonAccountPage(fullName, data.getEmail());
		SFPersonAccountPage sfPersonAccountPage = new SFPersonAccountPage(driver);

		// Soft assertion: check value but don't stop execution if it fails

		// Validate that the Account Name displayed matches the expected full name

		stepInfo("Validation of Person Account Information for : " + fullName);

		softAssert.assertEquals(sfPersonAccountPage.getAccountName(), fullName, "fullName mismatch");
		// Step 8
		passStep("Verified Full Name : " + sfPersonAccountPage.getAccountName());

		// Validate that the Email displayed matches the expected email from test data
		softAssert.assertEquals(sfPersonAccountPage.getEmail(), data.getEmail(), "Email mismatch");
		// Step 9
		passStep("Verified Email : " + sfPersonAccountPage.getEmail());

		// Validate expected Birth Date in MM/DD/YYYY format and validate it matches UI
		String birthDate = data.getMonth() + "/" + data.getDay() + "/" + data.getYear();
		softAssert.assertEquals(sfPersonAccountPage.getBirthDate(), birthDate, "birthDate mismatch");
		// Step 10
		passStep("Verified Birthdate : " + sfPersonAccountPage.getBirthDate());

		// Validate whether SSN presence flag (Yes/No) matches expected data
		softAssert.assertEquals(sfPersonAccountPage.getHasSSN(), data.getHasSsn(), "hasSSN mismatch");

		// Step 11
		passStep("Verified HasSSN : " + (sfPersonAccountPage.getHasSSN() ? "Yes" : "No"));

		// Validate that the SSN value matches expected SSN
		softAssert.assertEquals(sfPersonAccountPage.getSSN(), normalizeValue(data.getSsn()), "SSN mismatch");

		// Step 12
		passStep("Verified SSN : " + (sfPersonAccountPage.getSSN() == null ? "" : sfPersonAccountPage.getSSN()));

		// Validate whether ITIN presence flag (Yes/No) matches expected data
		softAssert.assertEquals(sfPersonAccountPage.getHasItin(), data.getHasItin(), "hasITIn mismatch");
		// Step 13
		passStep("Verified HasITIN : "
				+ (sfPersonAccountPage.getHasItin() == null ? "" : (sfPersonAccountPage.getHasItin() ? "Yes" : "No")));

		// Validate that the ITIN value matches expected ITIN

		softAssert.assertEquals(sfPersonAccountPage.getItin(), normalizeValue(data.getItin()), "ITIN mismatch");
		// Step 14
		passStep("Verified ITIN : " + (sfPersonAccountPage.getItin() == null ? "" : sfPersonAccountPage.getItin()));
		// Validate that the Alternate ID Type (e.g., Driver License, Passport) matches
		// expected value

		softAssert.assertEquals(sfPersonAccountPage.getAlternateIdType(), data.getIdType(),
				"Alternate ID Type mismatch");
		// Step 15
		passStep("Verified Alternate ID Type : "
				+ (sfPersonAccountPage.getAlternateIdType() == null ? "" : sfPersonAccountPage.getAlternateIdType()));

		// Validate that the National ID Number matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getNationalIDNumber(), data.getNationalId(),
				"National ID Number mismatch");

		// Step 16
		passStep("Verified National ID Number : "
				+ (sfPersonAccountPage.getNationalIDNumber() == null ? "" : sfPersonAccountPage.getNationalIDNumber()));

		// Validate that the Other ID Number matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getOtherIDNumber(), data.getOtherIdNumber(),
				"Other ID Number mismatch");
		// Step 17
		passStep("Verified Other ID Number : "
				+ (sfPersonAccountPage.getOtherIDNumber() == null ? "" : sfPersonAccountPage.getOtherIDNumber()));
		// Validate that the ID Issuer (issuing authority) matches expected value
		softAssert.assertEquals(sfPersonAccountPage.getIdIssuer(), data.getIdIssuer(), " ID Issuer  mismatch");
		// Step 18
		passStep("Verified ID Issuer: "
				+ (sfPersonAccountPage.getIdIssuer() == null ? "" : sfPersonAccountPage.getIdIssuer()));
		attachScreenshotToExtent("All fields are verified", "All fields are verified");
		softAssert.assertAll();

		stepInfo("All field are validated successfully for " + data.getScenario());

	}

}
