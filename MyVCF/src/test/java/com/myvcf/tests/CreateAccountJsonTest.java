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

public class CreateAccountJsonTest extends BaseTest {

	@DataProvider(name = "createAccountData")
	public Object[][] createAccountData() {
		List<CreateAccountData> rows = JsonDataReader.readResourceList("/data/createAccount.json",
				CreateAccountData[].class);
		return rows.stream().map(r -> new Object[] { r }).toArray(Object[][]::new);
	}

	private String maskId(String value) {
		if (value == null)
			return null;

		String digits = value.replaceAll("\\D", "");
		if (digits.length() <= 4)
			return digits;

		return "***-**-" + digits.substring(digits.length() - 4);
	}

	private void gotoCreateAccountPage() {
		openUrl(resolvedBaseUrlFromConfig());
		// Step 1

		passStep(" 📱 Opened MyVCF Portal App : " + ConfigReader.getActiveEnv());

		LoginPage loginPage = new LoginPage(driver);
		loginPage.clickCreateAccountButton();

		// Step 2
		passStep(" 📋 Opened Create Account Page");

	}

	private void openSalesforceInternal() {

		openUrl(ConfigReader.getSalesforceUrl("SIT"));

		SFLoginPage sfLoginPage = new SFLoginPage(driver);
		sfLoginPage.loginToSFInternal(ConfigReader.getSalesforceAdminUsername("SIT"),
				ConfigReader.getSalesforceAdminPassword("SIT"));
		// Step 5
		passStep(" 🌐 Opened MyVCF Internal App : " + ConfigReader.getActiveEnv());

	}

	private void goToPersonAccountPage(String fullName, String email) {

		SFHomePage sfHomePage = new SFHomePage(driver);
		sfHomePage.openAllAccount();
		// Step 6
		passStep(" 📖 Opened All Account from Accounts dropdown in HomePage");
		sfHomePage.searchForAccount(email);
		sfHomePage.clickPersonAccountLink(fullName);

		// Step 7
		passStep(" 👤 Opened Person Account for : " + fullName);

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
		passStep(" 📝 Filling out create account form");

		createAccountsPage.filloutCreateAccountForm(data);
		// Verify the success message

		Assert.assertTrue(createAccountsPage.successfullAccountCreationMessageIsDisplayed());
		// Step 4
		passStep(" ✔️ Account Created Successfully!");
		attachScreenshotToExtent("Screenshot", "Success Message Verified");

		// Navigate to MyVCF Internal

		openSalesforceInternal();

		String fullName = java.util.stream.Stream
				.of(data.getFirstName(), data.getMiddleName(), data.getLastName(), data.getSuffix())
				.filter(s -> s != null && !s.isBlank()).map(String::trim)
				.collect(java.util.stream.Collectors.joining(" "));

		goToPersonAccountPage(fullName, data.getEmail());
		SFPersonAccountPage sfPersonAccountPage = new SFPersonAccountPage(driver);

		// Soft assertion: check value but don't stop execution if it fails

		// Validate that the Account Name displayed matches the expected full name

		stepInfo(" 🔍 Validation of Person Account Information for : " + fullName);

		softAssert.assertEquals(sfPersonAccountPage.getAccountName().trim(), fullName.trim(), "fullName mismatch");

		// Step 8
		passStep(" ✔️ Verified Full Name : " + sfPersonAccountPage.getAccountName());

		// Validate that the Email displayed matches the expected email from test data
		softAssert.assertEquals(sfPersonAccountPage.getEmail(), data.getEmail(), "Email mismatch");
		// Step 9
		passStep(" ✔️ Verified Email : " + sfPersonAccountPage.getEmail());

		// Validate expected Birth Date in MM/DD/YYYY format and validate it matches UI
		String birthDate = data.getMonth() + "/" + data.getDay() + "/" + data.getYear();
		softAssert.assertEquals(sfPersonAccountPage.getBirthDate(), birthDate, "birthDate mismatch");
		// Step 10
		passStep(" ✔️ Verified Birthdate : " + sfPersonAccountPage.getBirthDate());

		if (Boolean.TRUE.equals(data.getHasSsn())) {

			softAssert.assertEquals(sfPersonAccountPage.getHasSSN(), true, "hasSSN mismatch");
			passStep(" ✔️ Verified HasSSN : Yes");

			softAssert.assertEquals(sfPersonAccountPage.getSSN(), normalizeValue(data.getSsn()), "SSN mismatch");
			passStep(" ✔️ Verified SSN : " + maskId(sfPersonAccountPage.getSSN()));

		}

		else if (Boolean.TRUE.equals(data.getHasItin())) {

			softAssert.assertEquals(sfPersonAccountPage.getHasItin(), true, "hasITIN mismatch");
			passStep(" ✔️ Verified HasITIN : Yes");

			softAssert.assertEquals(sfPersonAccountPage.getItin(), normalizeValue(data.getItin()), "ITIN mismatch");
			passStep(" ✔️ Verified ITIN : " + maskId(sfPersonAccountPage.getItin()));

		}

		else if ("National ID".equalsIgnoreCase(data.getIdType())) {

			softAssert.assertEquals(sfPersonAccountPage.getAlternateIdType(), data.getIdType(),
					"Alternate ID Type mismatch");
			passStep(" ✔️ Verified Alternate ID Type : " + sfPersonAccountPage.getAlternateIdType());

			softAssert.assertEquals(sfPersonAccountPage.getNationalIDNumber(), data.getNationalId(),
					"National ID mismatch");
			passStep(" ✔️ Verified National ID : " + maskId(sfPersonAccountPage.getNationalIDNumber()));

			softAssert.assertEquals(sfPersonAccountPage.getIdIssuer(), data.getIdIssuer(), "ID Issuer mismatch");
			passStep(" ✔️ Verified ID Issuer : " + sfPersonAccountPage.getIdIssuer());
		}

		else if ("Other ID".equalsIgnoreCase(data.getIdType())) {

			softAssert.assertEquals(sfPersonAccountPage.getAlternateIdType(), data.getIdType(),
					"Alternate ID Type mismatch");
			passStep(" ✔️ Verified Alternate ID Type : " + sfPersonAccountPage.getAlternateIdType());

			softAssert.assertEquals(sfPersonAccountPage.getOtherIDNumber(), data.getOtherIdNumber(),
					"Other ID mismatch");
			passStep(" ✔️ Verified Other ID Number : " + maskId(sfPersonAccountPage.getOtherIDNumber()));

			softAssert.assertEquals(sfPersonAccountPage.getIdIssuer(), data.getIdIssuer(), "ID Issuer mismatch");
			passStep(" ✔️ Verified ID Issuer : " + sfPersonAccountPage.getIdIssuer());
		}
		attachScreenshotToExtent("Screenshot", "All fields were verified");
		softAssert.assertAll();

		stepInfo(" 🏁✔️ All fields were verified successfully for the scenario:  " + data.getScenario());

	}

}
