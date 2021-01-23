package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Joseph on 3/26/2018.
 */

public class LoanRequestCreditClub implements Serializable {

    @SerializedName("LoanAmount")
    private double LoanAmount;

    @SerializedName("LoanProductID") 
    private int LoanProductID;

    @SerializedName("AssociationID") 
    private String AssociationID;

    @SerializedName("MemberID") 
    private String MemberID;

    @SerializedName("CustomerAccountNumber") 
    private String CustomerAccountNumber;

    @SerializedName("CustomerPhoneNumber") 
    private String CustomerPhoneNumber;

    @SerializedName("InstitutionCode") 
    private String InstitutionCode;

    @SerializedName("GeoLocation") 
    private String GeoLocation;

    @SerializedName("LoanProductInstitutionCode") 
    private String LoanProductInstitutionCode;

    @SerializedName("AgentPhoneNumber") 
    private String AgentPhoneNumber;

    @SerializedName("AgentPin") 
    private String AgentPin;

    @SerializedName("AdditionalInformation") 
    private String AdditionalInformation;

    public double getLoanAmount() {
        return LoanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        LoanAmount = loanAmount;
    }

    public int getLoanProductID() {
        return LoanProductID;
    }

    public void setLoanProductID(int loanProductID) {
        LoanProductID = loanProductID;
    }

    public String getAssociationID() {
        return AssociationID;
    }

    public void setAssociationID(String associationID) {
        AssociationID = associationID;
    }

    public String getMemberID() {
        return MemberID;
    }

    public void setMemberID(String memberID) {
        MemberID = memberID;
    }

    public String getCustomerAccountNumber() {
        return CustomerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        CustomerAccountNumber = customerAccountNumber;
    }

    public String getCustomerPhoneNumber() {
        return CustomerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        CustomerPhoneNumber = customerPhoneNumber;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getLoanProductInstitutionCode() {
        return LoanProductInstitutionCode;
    }

    public void setLoanProductInstitutionCode(String loanProductInstitutionCode) {
        LoanProductInstitutionCode = loanProductInstitutionCode;
    }

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
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
}
