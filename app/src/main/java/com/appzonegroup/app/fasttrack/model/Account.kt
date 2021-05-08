package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 5/26/2016.
 */
public class Account {
    private long ID;
    private String gender;//
    private String phoneNo;//
    private String lastName;//
    private String firstName;//
    private String otherNames;//
    private String religion;//
    private String email;//
    private String state;//
    private String accountOfficercode;//
    private String nationalIdentityNo;//
    private String referralName;
    private String referralPhoneNo;
    private String customerPassportInBytes;
    private String customerSignatureInBytes;
    private String secondaryIdentityInBytes;
    private String dateOfBirth;//
    private String address;//
    private String placeOfBirth;//
    private String bvn;//
    private String productCode;//





    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAccountofficercode() {
        return accountOfficercode;
    }

    public void setAccountofficercode(String accountofficercode) {
        this.accountOfficercode = accountofficercode;
    }

    public String getNationalIdentityNo() {
        return nationalIdentityNo;
    }

    public void setNationalIdentityNo(String nationalIdentityNo) {
        this.nationalIdentityNo = nationalIdentityNo;
    }

    public String getReferralName() {
        return referralName;
    }

    public void setReferralName(String referralName) {
        this.referralName = referralName;
    }

    public String getReferralPhoneNo() {
        return referralPhoneNo;
    }

    public void setReferralPhoneNo(String referralPhoneNo) {
        this.referralPhoneNo = referralPhoneNo;
    }

    public String getCustomerPassportInBytes() {
        return customerPassportInBytes;
    }

    public void setCustomerPassportInBytes(String customerPassportInBytes) {
        this.customerPassportInBytes = customerPassportInBytes;
    }

    public String getCustomerSignatureInBytes() {
        return customerSignatureInBytes;
    }

    public void setCustomerSignatureInBytes(String customerSignatureInBytes) {
        this.customerSignatureInBytes = customerSignatureInBytes;
    }

    public String getSecondaryIdentityInBytes() {
        return secondaryIdentityInBytes;
    }

    public void setSecondaryIdentityInBytes(String secondaryIdentityInBytes) {
        this.secondaryIdentityInBytes = secondaryIdentityInBytes;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }


    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Customer getCustomer()
    {
        Customer customer = new Customer();
        customer.setProductCode(getProductCode());
        customer.setProductName("");
        //customer.setAccountNumber("4567890987");
        customer.setPIN("");
        customer.setCustomerLastName(getLastName());
        customer.setCustomerFirstName(getFirstName());
        customer.setCustomerPhoneNumber(getPhoneNo());
        customer.setGender(getGender());
        //customer.setStarterPackNumber(getCardSerialNumber());
        customer.setBVN(getBvn());
        customer.setAddress(getAddress());
        customer.setPlaceOfBirth(getPlaceOfBirth());
        customer.setDateOfBirth(getDateOfBirth());
        //customer.setGeoLocation(getGeoLocation());

        return customer;
    }


    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
