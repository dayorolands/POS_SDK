package com.appzonegroup.app.fasttrack.model;

/**
 * Created by madunaguekenedavid on 11/04/2018.
 */

public class BalanceEnquiry {
    private String AdditionalInformation;
    private String AgentPin;
    private String CustomerAccountNumber;
    private String AgentPhoneNumber;

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    private String GeoLocation;
    private String InstitutionCode;

    public String getAdditionalInformation() {
        return AdditionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        AdditionalInformation = additionalInformation;
    }

    public String getAgentPin() {
        return AgentPin;
    }

    public void setAgentPin(String agentPin) {
        AgentPin = agentPin;
    }


    public String getCustomerAccountNumber() {
        return CustomerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        CustomerAccountNumber = customerAccountNumber;
    }

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }



}
