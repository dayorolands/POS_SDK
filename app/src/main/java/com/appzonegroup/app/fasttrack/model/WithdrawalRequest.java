package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 17/07/2017.
 */

public class WithdrawalRequest {

    private String AgentPhoneNumber;
    private String InstitutionCode;

    private String CustomerAccountNumber;
    private String AgentPin;

    private String Token;
    private String CustomerPin = "0000";
    private String Amount;

    private String GeoLocation;
    private String AdditionalInformation;

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getCustomerAccountNumber() {
        return CustomerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        CustomerAccountNumber = customerAccountNumber;
    }

    public String getAgentPin() {
        return AgentPin;
    }

    public void setAgentPin(String agentPin) {
        AgentPin = agentPin;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getCustomerPin() {
        return CustomerPin;
    }

    public void setCustomerPin(String customerPin) {
        CustomerPin = customerPin;
    }

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public String getAdditionalInformation() {
        return AdditionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        AdditionalInformation = additionalInformation;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }
}
