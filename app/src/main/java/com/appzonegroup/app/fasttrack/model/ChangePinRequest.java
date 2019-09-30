package com.appzonegroup.app.fasttrack.model;

/**
 * Created by madunaguekenedavid on 12/04/2018.
 */

public class ChangePinRequest {
      private String AgentPhoneNumber;
      private String InstitutionCode;
      private String ActivationCode;
      private String NewPin;
      private String ConfirmNewPin;
      private String GeoLocation;
      private String OldPin;
      private String CustomerPhoneNumber;
      private String AgentPin;
      private String CustomerToken;

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

    public String getActivationCode() {
        return ActivationCode;
    }

    public void setActivationCode(String activationCode) {
        ActivationCode = activationCode;
    }

    public String getNewPin() {
        return NewPin;
    }

    public void setNewPin(String newPin) {
        NewPin = newPin;
    }

    public String getConfirmNewPin() {
        return ConfirmNewPin;
    }

    public void setConfirmNewPin(String confirmNewPin) {
        ConfirmNewPin = confirmNewPin;
    }

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public String getOldPin() {
        return OldPin;
    }

    public void setOldPin(String oldPin) {
        OldPin = oldPin;
    }

    public String getCustomerPhoneNumber() {
        return CustomerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        CustomerPhoneNumber = customerPhoneNumber;
    }

    public String getAgentPin() {
        return AgentPin;
    }

    public void setAgentPin(String agentPin) {
        AgentPin = agentPin;
    }

    public String getCustomerToken() {
        return CustomerToken;
    }

    public void setCustomerToken(String customerToken) {
        CustomerToken = customerToken;
    }
}
