package com.myvcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountData {
	private String scenario;
	private String firstName;
	private String middleName;
	private String lastName;
	private String suffix;
	private String email;
	private String year;
	private String month;
	private String day;
	private Boolean hasSsn;
	private String ssn;
	private String confirmSsn;
	private Boolean hasItin;
	private String itin;
	private String confirmItin;
	private String idType;
	private String nationalId;
	private String confirmNationalId;
	private String idIssuer;
	private String otherIdNumber;
	private String confirmOtherIdNumber;
	

}
