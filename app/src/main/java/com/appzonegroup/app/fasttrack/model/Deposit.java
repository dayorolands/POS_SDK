package com.appzonegroup.app.fasttrack.model;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Oto-obong on 17/07/2017.
 */

public class Deposit {


    private String Name;
    private Long ID;
    private String AccountNumber;
    private String Amount;
    private String Narration;
    private String Reference;
    private Timestamp Date;


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public java.util.Date getDate() {
        return Date;
    }

    public void setDate(String date) {


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

         Date = new java.sql.Timestamp(parsedDate.getTime());


    }

    public String getNarration() {
        return Narration;
    }

    public void setNarration(String narration) {
        Narration = narration;
    }

    public String getReference() {
        return Reference;
    }

    public void setReference(String reference) {
        Reference = reference;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }
}
