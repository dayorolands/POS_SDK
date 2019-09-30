package com.appzonegroup.app.fasttrack.model;

import java.io.Serializable;

/**
 * Created by Oto-obong on 20/08/2017.
 */

public class Beneficiary implements Serializable {

    private String AccountNumber;
    private String Address;
    private String AgentPhoneNumber;
    private String DateOfBirth;
    private Double EligibleAmount;
    private String FirstName;
    private int Gender;
    private String LastName;
    private String MiddleName;
    private Boolean Paid;
    private String PhoneNumber;
    private String TrackingReference;
    private int ID;
    private String Photo;
    private String Sync;


    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public Double getEligibleAmount() {
        return EligibleAmount;
    }

    public void setEligibleAmount(Double eligibleAmount) {
        EligibleAmount = eligibleAmount;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public int getGender() {
        return Gender;
    }

    public void setGender(int gender) {
        Gender = gender;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public Boolean getPaid() {
        return Paid;
    }

    public void setPaid(Boolean paid) {
        Paid = paid;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getTrackingReference() {
        return TrackingReference;
    }

    public void setTrackingReference(String trackingReference) {
        TrackingReference = trackingReference;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getSync() {
        return Sync;
    }

    public void setSync(String sync) {
        Sync = sync;
    }
}
