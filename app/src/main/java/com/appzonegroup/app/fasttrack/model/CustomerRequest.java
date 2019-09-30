package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/20/2018.
 */

public class CustomerRequest {

    private String CustomerLastName;
    private String CustomerFirstName;
    private String CustomerPhoneNumber;
    private String Gender;
    private String Address;
    private String DateOfBirth;
    private String PlaceOfBirth;

    private String NOKPhone;
    private String NOKName;

    private String StarterPackNumber;
    private String ProductCode;
    private String ProductName;
    private String AccountNumber;
    private String PIN;
    private String BVN;

    private String GeoLocation;
    private String AgentPhoneNumber;
    private String AgentPin;
    private String InstitutionCode;

    private String AdditionalInformation;

    public String getUniqueReferenceID() {
        return UniqueReferenceID;
    }

    public void setUniqueReferenceID(String uniqueReferenceID) {
        UniqueReferenceID = uniqueReferenceID;
    }

    private String UniqueReferenceID;

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getCustomerLastName() {
        return CustomerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        CustomerLastName = customerLastName;
    }

    public String getCustomerFirstName() {
        return CustomerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        CustomerFirstName = customerFirstName;
    }

    public String getCustomerPhoneNumber() {
        return CustomerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        CustomerPhoneNumber = customerPhoneNumber;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getStarterPackNumber() {
        return StarterPackNumber;
    }

    public void setStarterPackNumber(String starterPackNumber) {
        StarterPackNumber = starterPackNumber;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getNOKPhone() {
        return NOKPhone;
    }

    public void setNOKPhone(String NOKPhone) {
        this.NOKPhone = NOKPhone;
    }

    public String getNOKName() {
        return NOKName;
    }

    public void setNOKName(String NOKName) {
        this.NOKName = NOKName;
    }

    public String getPlaceOfBirth() {
        return PlaceOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        PlaceOfBirth = placeOfBirth;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public String getBVN() {
        return BVN;
    }

    public void setBVN(String BVN) {
        this.BVN = BVN;
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


    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getAdditionalInformation() {
        return AdditionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        AdditionalInformation = additionalInformation;
    }
}
