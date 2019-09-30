package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/27/2018.
 */

public class NameEnquiryByAccountNumberResponse {

    private String AccountName;
    private String Number;
    private String PhoneNumber;

    public String getAccountName() {
        return AccountName;
    }

    public void setAccountName(String accountName) {
        AccountName = accountName;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
