package com.appzonegroup.app.fasttrack.model;

/**
 * Created by madunaguekenedavid on 11/04/2018.
 */

public class Balance {

    private String ResponseMessage;
    private double AvailableBalance;
    private double Balance;


    private boolean IsSussessful;

    public boolean isSussessful() {
        return IsSussessful;
    }

    public void setSussessful(boolean sussessful) {
        IsSussessful = sussessful;
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }

    public double getAvailableBalance() {
        return AvailableBalance;
    }

    public void setAvailableBalance(double availableBalance) {
        AvailableBalance = availableBalance;
    }

    public double getBalance() {
        return Balance;
    }

    public void setBalance(double balance) {
        Balance = balance;
    }

}
