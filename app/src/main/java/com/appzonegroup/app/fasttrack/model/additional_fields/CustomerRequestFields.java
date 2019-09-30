package com.appzonegroup.app.fasttrack.model.additional_fields;

public class CustomerRequestFields {

    private String MiddleName;
    private int Province;
    private String Occupation;
    private String Passport;
    private String Signature;
    private String IDCard;

    public String getMiddleName() {
        return MiddleName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public int getProvince() {
        return Province;
    }

    public void setProvince(int province) {
        Province = province;
    }

    public String getOccupation() {
        return Occupation;
    }

    public void setOccupation(String occupation) {
        Occupation = occupation;
    }

    public String getPassport() {
        return Passport;
    }

    public void setPassport(String passport) {
        Passport = passport;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    public String getIDCard() {
        return IDCard;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }
}
