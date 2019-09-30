package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 6/3/2016.
 */
public class BVNRequest {

    private long ID;
    private String BVN ;
    private String CustomerPhoneNumber ;
    private String CustomerAccountNumber ;
    private String CustomerPIN ;
    private String InstitutionCode ;
    private String IsSync;
    private String Remark;
    private String IsConfirmed;
    private String GeoLocation;

    private String AgentPhoneNumber;
    private String AgentPin;
    private String AdditionalInformation;

    public String getBVN() {
        return BVN;
    }

    public void setBVN(String BVN) {
        this.BVN = BVN;
    }

    public String getCustomerPhoneNumber() {
        return CustomerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
       CustomerPhoneNumber = customerPhoneNumber;
    }

    public String getCustomerAccountNumber() {
        return CustomerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        CustomerAccountNumber = customerAccountNumber;
    }

    public String getCustomerPIN() {
        return CustomerPIN;
    }

    public void setCustomerPIN(String customerPIN) {
        CustomerPIN = customerPIN;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getIsSync() {
        return IsSync;
    }

    public void setIsSync(String isSync) {
        IsSync = isSync;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getIsConfirmed() {
        return IsConfirmed;
    }

    public void setIsConfirmed(String isConfirmed) {
        IsConfirmed = isConfirmed;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
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
