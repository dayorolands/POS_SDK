package com.appzonegroup.app.fasttrack.model.jsonbody;

import java.io.Serializable;

/**
 * Created by Oto-obong on 25/09/2017.
 */

public class PayBillItemModel implements Serializable {


    private String agentPhone;

    private String agentPIN;

    private String institutionCode;

    private String otp;

    private String merchantBillerIdField;

    private String billItemID;

    private String billCategoryID;

    private String customerAccountNumber;

    private String amount;

    private String email;

    private String customerPhoneNumber;

    private String customerID;

    public String getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(String agentPhone) {
        this.agentPhone = agentPhone;
    }

    public String getAgentPIN() {
        return agentPIN;
    }

    public void setAgentPIN(String agentPIN) {
        this.agentPIN = agentPIN;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMerchantBillerIdField() {
        return merchantBillerIdField;
    }

    public void setMerchantBillerIdField(String merchantBillerIdField) {
        this.merchantBillerIdField = merchantBillerIdField;
    }

    public String getBillItemID() {
        return billItemID;
    }

    public void setBillItemID(String billItemID) {
        this.billItemID = billItemID;
    }

    public String getBillCategoryID() {
        return billCategoryID;
    }

    public void setBillCategoryID(String billCategoryID) {
        this.billCategoryID = billCategoryID;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
}
