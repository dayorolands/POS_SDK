package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/16/2018.
 */

public class NameEnquiryResponse {

    private String BeneficiaryAccountName;
    private String NameEnquirySessionID;
    private String BeneficiaryBVN;
    private String BeneficiaryKYC;
    private String ResponseMessage;
    private boolean Status;

    public String getBeneficiaryAccountName() {
        return BeneficiaryAccountName;
    }

    public void setBeneficiaryAccountName(String beneficiaryAccountName) {
        BeneficiaryAccountName = beneficiaryAccountName;
    }

    public String getNameEnquirySessionID() {
        return NameEnquirySessionID;
    }

    public void setNameEnquirySessionID(String nameEnquirySessionID) {
        NameEnquirySessionID = nameEnquirySessionID;
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

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }
}
