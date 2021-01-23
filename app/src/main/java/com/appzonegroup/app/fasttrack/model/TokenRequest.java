package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Joseph on 3/22/2018.
 */

public class TokenRequest implements Serializable {

    @SerializedName("AgentPhoneNumber")
    private String AgentPhoneNumber;

    @SerializedName("InstitutionCode")
    private String InstitutionCode;

    @SerializedName("AdditionalInformation")
    private String AdditionalInformation;

    @SerializedName("Amount")
    private String Amount;

    @SerializedName("CustomerAccountNumber")
    private String CustomerAccountNumber;

    @SerializedName("AgentPin")
    private String AgentPin;

    @SerializedName("IsPinChange")
    private boolean IsPinChange;

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    public String getCustomerAccountNumber() {
        return CustomerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        CustomerAccountNumber = customerAccountNumber;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getAgentPin() {
        return AgentPin;
    }

    public void setAgentPin(String agentPin) {
        AgentPin = agentPin;
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

    public boolean isPinChange() {
        return IsPinChange;
    }

    public void setPinChange(boolean pinChange) {
        IsPinChange = pinChange;
    }
}
