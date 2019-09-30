package com.appzonegroup.app.fasttrack.model;

import com.appzonegroup.app.fasttrack.utility.Misc;

/**
 * Created by Joseph on 7/19/2017.
 */
public class ReportItem {

    private String To;// "0007969630",
    private String From;// "0007969630",
    private String CustomerID;// null,
    private String CustomerName;// null,
    private String CustomerPhone;// null,
    private String ProductName;// null,
    private String ProductCode;// null,
    private double Amount;// 0,
    private String Date;// "2017-07-05T11:32:23",
    private int TransactionTypeID;// 315345,
    private int TransactionTypeName;// 1,
    private String FromPhoneNumber;// "08026319666",
    private String EncryptedPIN;// null,
    private String STAN;// "817319",
    private String SwitchTransactionTime;// "0001-01-01T00:00:00",
    private boolean IsActive;// false,
    private String DisplayMessage;// null,
    private long ID;// 895289

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public String getDate() {
        return Date;
    }

    public String getFormatedDate(){
        java.util.Date myTime = Misc.serverTimetoDate(getDate());
        return Misc.dateToShortString(myTime);
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getTransactionTypeID() {
        return TransactionTypeID;
    }

    public void setTransactionTypeID(int transactionTypeID) {
        TransactionTypeID = transactionTypeID;
    }

    public int getTransactionTypeName() {
        return TransactionTypeName;
    }

    public void setTransactionTypeName(int transactionTypeName) {
        TransactionTypeName = transactionTypeName;
    }

    public String getFromPhoneNumber() {
        return FromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        FromPhoneNumber = fromPhoneNumber;
    }

    public String getEncryptedPIN() {
        return EncryptedPIN;
    }

    public void setEncryptedPIN(String encryptedPIN) {
        EncryptedPIN = encryptedPIN;
    }

    public String getSTAN() {
        return STAN;
    }

    public void setSTAN(String STAN) {
        this.STAN = STAN;
    }

    public String getSwitchTransactionTime() {
        return SwitchTransactionTime;
    }

    public void setSwitchTransactionTime(String switchTransactionTime) {
        SwitchTransactionTime = switchTransactionTime;
    }

    public boolean isActive() {
        return IsActive;
    }

    public void setActive(boolean active) {
        IsActive = active;
    }

    public String getDisplayMessage() {
        return DisplayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        DisplayMessage = displayMessage;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
