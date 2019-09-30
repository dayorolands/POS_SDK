package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/13/2018.
 */

public class FundsTransferRequest {

    private String ExternalTransactionReference;
    private String BeneficiaryAccountNumber;
    private String BeneficiaryAccountName;
    private String BeneficiaryBVN;
    private String BeneficiaryKYC;
    private String NameEnquirySessionID;
    private String BeneficiaryInstitutionCode;
    private String AuthToken;
    private String AgentPhoneNumber;
    private String AgentPin;
    private String GeoLocation;
    private double AmountInNaira;
    private boolean IsToRelatedCommercialBank; //for transfer to sterling bank this is set to true
    private String Narration;

    public String getExternalTransactionReference() {
        return ExternalTransactionReference;
    }

    public void setExternalTransactionReference(String externalTransactionReference) {
        ExternalTransactionReference = externalTransactionReference;
    }

    public String getBeneficiaryAccountNumber() {
        return BeneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        BeneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getBeneficiaryInstitutionCode() {
        return BeneficiaryInstitutionCode;
    }

    public void setBeneficiaryInstitutionCode(String beneficiaryInstitutionCode) {
        BeneficiaryInstitutionCode = beneficiaryInstitutionCode;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
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

    public String getGeoLocation() {
        return GeoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        GeoLocation = geoLocation;
    }

    public double getAmountInNaira() {
        return AmountInNaira;
    }

    public void setAmountInNaira(double amountInNaira) {
        AmountInNaira = amountInNaira;
    }

    public boolean isToRelatedCommercialBank() {
        return IsToRelatedCommercialBank;
    }

    public void setToRelatedCommercialBank(boolean toRelatedCommercialBank) {
        IsToRelatedCommercialBank = toRelatedCommercialBank;
    }

    public String getBeneficiaryAccountName() {
        return BeneficiaryAccountName;
    }

    public void setBeneficiaryAccountName(String beneficiaryAccountName) {
        BeneficiaryAccountName = beneficiaryAccountName;
    }

    public String getBeneficiaryBVN() {
        return BeneficiaryBVN;
    }

    public void setBeneficiaryBVN(String beneficiaryBVN) {
        BeneficiaryBVN = beneficiaryBVN;
    }

    public String getBeneficiaryKYC() {
        return BeneficiaryKYC;
    }

    public void setBeneficiaryKYC(String beneficiaryKYC) {
        BeneficiaryKYC = beneficiaryKYC;
    }

    public String getNameEnquirySessionID() {
        return NameEnquirySessionID;
    }

    public void setNameEnquirySessionID(String nameEnquirySessionID) {
        NameEnquirySessionID = nameEnquirySessionID;
    }

    public String getNarration() {
        return Narration;
    }

    public void setNarration(String narration) {
        Narration = narration;
    }
}
