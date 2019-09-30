package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 6/3/2016.
 */
public class LoanRequest {

    private String Amount;
    private String LoanProductID ;
    private String AssociationID ;
    private String MemberID ;
    private String CustomerAccountNumber ;
    private String PhoneNumber;
    private String InstitutionCode ;
    private String Tenure ;
    private String FundProviderCode;
    private String AgentPhoneNumber;
    private String CustomerID;
    private String CustomerName;
    private String BVN;
    private String AgentPin;
    private String GeoLocation;

    public String getAgentPin() {
        return AgentPin;
    }

    public void setAgentPin(String agentPin) {
        AgentPin = agentPin;
    }

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getLoanProductID() {
        return LoanProductID;
    }

    public void setLoanProductID(String loanProductID) {
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

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }


    public String getTenure() {
        return Tenure;
    }

    public void setTenure(String tenure) {
        Tenure = tenure;
    }

    public String getFundProviderCode() {
        return FundProviderCode;
    }

    public void setFundProviderCode(String fundProviderCode) {
        FundProviderCode = fundProviderCode;
    }

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getBVN() {
        return BVN;
    }

    public void setBVN(String BVN) {
        this.BVN = BVN;
    }
}
