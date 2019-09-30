package com.appzonegroup.app.fasttrack.model;

import android.content.Context;

import com.appzonegroup.app.fasttrack.utility.LocalStorage;

/**
 * Created by Joseph on 6/5/2016.
 */
public class MobileAccount {

    private String CustomerID;
    private String LastName;
    private String OtherNames;
    private String MobilePhone;
    private String PIN;
    private String PINOffset;
    private String ActivationCode;
    private int PinTries;
    private boolean IsAgentAccount;
    private int MobileAccountStatus ;
    private int OpenedBy ;
    private long   RegistrarID ;
    private String RegistrarCode ;
    private String RegistrarName ;
    private String ProductCode ;
    private String ProductName ;
    private String DateCreated ;
    private String PersonalPhoneNumber ;
    private String DateActivated ;
    private String InstitutionCode ;
    private String StandardPhoneNumber;
    private int TheGender ;
    private int AccountRestriction ;
    private boolean ActivateViaJava ;
    private boolean ActivateViaUSSD ;
    private String ReferalPhone ;
    private String StarterPackNo ;
    private String PlaceOfBirth ;
    private String idNo ;
    private String NOKPhone ;
    private String NOKName ;
    private String Address ;
    private String NUBAN ;
    private String DateOfBirth ;
    private boolean HasSufficientInfo ;
    private boolean Verified ;

    public MobileAccount(){}

    public static MobileAccount GetInstance(Context context)
    {
        MobileAccount mobileAccount = new MobileAccount();
        mobileAccount.setInstitutionCode(LocalStorage.getInstitutionCode(context));
        mobileAccount.setMobilePhone(LocalStorage.getPhoneNumber(context));
        mobileAccount.setActivationCode(LocalStorage.GetValueFor(AppConstants.AGENT_CODE, context));
        mobileAccount.setPIN(LocalStorage.GetValueFor(AppConstants.AGENT_PIN, context));
        return mobileAccount;

    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getOtherNames() {
        return OtherNames;
    }

    public void setOtherNames(String otherNames) {
        OtherNames = otherNames;
    }

    public String getMobilePhone() {
        return MobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        MobilePhone = mobilePhone;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getPINOffset() {
        return PINOffset;
    }

    public void setPINOffset(String PINOffset) {
        this.PINOffset = PINOffset;
    }

    public String getActivationCode() {
        return ActivationCode;
    }

    public void setActivationCode(String activationCode) {
        ActivationCode = activationCode;
    }

    public int getPinTries() {
        return PinTries;
    }

    public void setPinTries(int pinTries) {
        PinTries = pinTries;
    }

    public boolean isAgentAccount() {
        return IsAgentAccount;
    }

    public void setAgentAccount(boolean agentAccount) {
        IsAgentAccount = agentAccount;
    }

    public int getMobileAccountStatus() {
        return MobileAccountStatus;
    }

    public void setMobileAccountStatus(int mobileAccountStatus) {
        MobileAccountStatus = mobileAccountStatus;
    }

    public int getOpenedBy() {
        return OpenedBy;
    }

    public void setOpenedBy(int openedBy) {
        OpenedBy = openedBy;
    }

    public long getRegistrarID() {
        return RegistrarID;
    }

    public void setRegistrarID(long registrarID) {
        RegistrarID = registrarID;
    }

    public String getRegistrarCode() {
        return RegistrarCode;
    }

    public void setRegistrarCode(String registrarCode) {
        RegistrarCode = registrarCode;
    }

    public String getRegistrarName() {
        return RegistrarName;
    }

    public void setRegistrarName(String registrarName) {
        RegistrarName = registrarName;
    }

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

    public String getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public String getPersonalPhoneNumber() {
        return PersonalPhoneNumber;
    }

    public void setPersonalPhoneNumber(String personalPhoneNumber) {
        PersonalPhoneNumber = personalPhoneNumber;
    }

    public String getDateActivated() {
        return DateActivated;
    }

    public void setDateActivated(String dateActivated) {
        DateActivated = dateActivated;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getStandardPhoneNumber() {
        return StandardPhoneNumber;
    }

    public void setStandardPhoneNumber(String standardPhoneNumber) {
        StandardPhoneNumber = standardPhoneNumber;
    }

    public int getTheGender() {
        return TheGender;
    }

    public void setTheGender(int theGender) {
        TheGender = theGender;
    }

    public int getAccountRestriction() {
        return AccountRestriction;
    }

    public void setAccountRestriction(int accountRestriction) {
        AccountRestriction = accountRestriction;
    }

    public boolean isActivateViaJava() {
        return ActivateViaJava;
    }

    public void setActivateViaJava(boolean activateViaJava) {
        ActivateViaJava = activateViaJava;
    }

    public boolean isActivateViaUSSD() {
        return ActivateViaUSSD;
    }

    public void setActivateViaUSSD(boolean activateViaUSSD) {
        ActivateViaUSSD = activateViaUSSD;
    }

    public String getReferalPhone() {
        return ReferalPhone;
    }

    public void setReferalPhone(String referalPhone) {
        ReferalPhone = referalPhone;
    }

    public String getStarterPackNo() {
        return StarterPackNo;
    }

    public void setStarterPackNo(String starterPackNo) {
        StarterPackNo = starterPackNo;
    }

    public String getPlaceOfBirth() {
        return PlaceOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        PlaceOfBirth = placeOfBirth;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
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

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getNUBAN() {
        return NUBAN;
    }

    public void setNUBAN(String NUBAN) {
        this.NUBAN = NUBAN;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public boolean isHasSufficientInfo() {
        return HasSufficientInfo;
    }

    public void setHasSufficientInfo(boolean hasSufficientInfo) {
        HasSufficientInfo = hasSufficientInfo;
    }

    public boolean isVerified() {
        return Verified;
    }

    public void setVerified(boolean verified) {
        Verified = verified;
    }
}
