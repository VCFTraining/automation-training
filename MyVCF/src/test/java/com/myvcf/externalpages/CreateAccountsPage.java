package com.myvcf.externalpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.myvcf.pages.common.BasePage;
import com.myvcf.pojo.CreateAccountData;

public class CreateAccountsPage extends BasePage {
	private final By createAccountLocator = By.xpath("//span[contains(text(), 'Create account')]//parent::button");
	private final By successMessageLocator = By.xpath("//strong[contains(text(), 'Success')]");

	public CreateAccountsPage(WebDriver driver) {
		super(driver);
	}
	
	//*[@role='option' and @data-value = 'Sr.']

	public void filloutCreateAccountForm(CreateAccountData data) {
		enterFirstName(data.getFirstName());
		enterMiddleName(data.getMiddleName());
		enterLastName(data.getLastName());
		enterSuffix(data.getSuffix());
		enterEmail(data.getEmail());
		enterDateOfBirth(data.getYear(), data.getMonth(), data.getDay());
		enterConfirmDateOfBirth(data.getYear(), data.getMonth(), data.getDay());
		enterSsn(data.getHasSsn(), data.getSsn(), data.getConfirmSsn());
		enterItin(data.getHasItin(), data.getItin(), data.getConfirmItin());
		enterNationalId(data.getIdType(), data.getNationalId(), data.getConfirmNationalId(), data.getIdIssuer());
		enterOtherId(data.getIdType(), data.getOtherIdNumber(), data.getConfirmOtherIdNumber(), data.getIdIssuer());
		clickCreateAccountButton();

	}

	public void enterFirstName(String firstName) {
		clearAndTypeByLabel(firstName, "First name");
	}

	public void enterMiddleName(String middleName) {
		clearAndTypeByLabel(middleName, "Middle name");
	}

	public void enterLastName(String lastName) {
		clearAndTypeByLabel(lastName, "Last name");
	}

	public void enterSuffix(String suffix) {
		if (suffix == null) {
			return;
		}
		
		selectByVisibleTextNearLabel("Suffix", suffix);
	}

	public void enterEmail(String email) {
//		int random = (int)(Math.random()*900) + 100;
//		email = email.replaceAll("\\+\\d{3}@", "+" +random+"@");
//		System.out.println("the new random email is " + email);
		clearAndTypeByLabel(email, "Email");
	}

	public void enterDateOfBirth(String year, String month, String day) {
		String dob = month + "-" + day + "-" + year;
		System.out.println("Date of Birth is  " + dob);
		clearAndTypeByLabel(dob, "Date of Birth");
	}

	public void enterConfirmDateOfBirth(String year, String month, String day) {
		String confirmDob = month + "-" + day + "-" + year;
		System.out.println("Date of Birth is  " + confirmDob);
		clearAndTypeByLabel(confirmDob, "Confirm date of birth");

	}

	public void enterSsn(Boolean hasSsn, String ssn, String confirmSsn) {
//		int middleTwo = (int)(Math.random()*90) +10;
//		int lastFour = (int)(Math.random()*9000) +1000;
//		ssn = "666 -" + middleTwo + "-"+ lastFour;
		if (hasSsn == null) {
			return;
		}
		if (hasSsn) {
			clickRadio("Do you have a U.S. Social Security Number (SSN)?", "Yes");
			enterMaskedDigitsByLabel(ssn, "Social Security Number (SSN)");
			enterMaskedDigitsByLabel(confirmSsn, "Confirm Social Security Number");

		} else {
			clickRadio("Do you have a U.S. Social Security Number (SSN)?", "No");
		}

	}

	public void enterItin(Boolean hasItin, String itin, String confirmItin) {
		if (hasItin == null) {
			return;
		}
		if (hasItin) {
			clickRadio("Do you have an Individual Taxpayer Identification Number (ITIN) issued by the United States",
					"Yes");
			enterMaskedDigitsByLabel(itin.substring(1), "Individual Taxpayer Identification Number (ITIN)");
			enterMaskedDigitsByLabel(confirmItin.substring(1), "Confirm Individual Taxpayer Identification Number");

		} else {
			clickRadio("Do you have an Individual Taxpayer Identification Number (ITIN) issued by the United States",
					"No");
		}

	}

	public void enterNationalId(String idType, String nationalId, String confirmNationalId, String idIssuer) {
		if (idType == null || idType.equals("Other ID")) {
			return;
		}
		clickRadio("What type of identification number do you have?", "National ID");
		clearAndTypeByLabel(nationalId, "National ID");
		clearAndTypeByLabel(confirmNationalId, "Confirm National ID");
		clearAndTypeByLabel(idIssuer, "ID issuer");
	}

	public void enterOtherId(String idType, String otherId, String confirmOtherId, String idIssuer) {
		if (idType == null || idType.equals("National ID")) {
			return;
		}
		clickRadio("What type of identification number do you have?", "Other ID");
		clearAndTypeByLabel(otherId, "Other ID");
		clearAndTypeByLabel(confirmOtherId, "Confirm Other ID");
		clearAndTypeByLabel(idIssuer, "ID issuer");
	}

	public void clickCreateAccountButton() {
		WebElement createAccountButton = driver.findElement(createAccountLocator);

		safeClick(createAccountButton);
	}

	public boolean successfullAccountCreationMessageIsDisplayed() {
		WebElement successMessage = waitVisible(successMessageLocator);
		return successMessage.isDisplayed();
	}

}
