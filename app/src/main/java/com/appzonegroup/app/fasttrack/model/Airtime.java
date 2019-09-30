package com.appzonegroup.app.fasttrack.model;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Oto-obong on 17/07/2017.
 */

public class Airtime {

    private long ID;
    private String Phone;
    private String Amount;
    private String Network;
    private Timestamp Date;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        this.Phone = phone;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        this.Amount = amount;
    }

    public String getNetwork() {
        return Network;
    }

    public void setNetwork(String network) {
        this.Network = network;
    }

    public Timestamp getDateTime() {
        return Date;
    }

    public void setDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        java.util.Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date = new java.sql.Timestamp(parsedDate.getTime());

    }
}
