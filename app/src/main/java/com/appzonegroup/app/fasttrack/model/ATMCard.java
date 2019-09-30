package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 12/14/2017.
 */

public class ATMCard {

    private String PAN;
    private String CVV;
    private String ExpiryDate;
    private String PIN;


    public ATMCard(){}

    public ATMCard(String PAN, String CVV, String expiryDate, String PIN)
    {
        setPAN(PAN);
        setCVV(CVV);
        setExpiryDate(expiryDate);
        setPIN(PIN);
    }
    public String getPAN() {
        return PAN;
    }

    public void setPAN(String PAN) {
        this.PAN = PAN;
    }

    public String getCVV() {
        return CVV;
    }

    public void setCVV(String CVV) {
        this.CVV = CVV;
    }

    public String getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.ExpiryDate = expiryDate;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }
}
