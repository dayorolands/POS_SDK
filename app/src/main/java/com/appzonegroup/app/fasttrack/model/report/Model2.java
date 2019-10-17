package com.appzonegroup.app.fasttrack.model.report;

import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.utility.Misc;

/**
 * Model class for:
 * i. Funds Transfer Commercial Bank report
 * ii. KiaKia To Sterling Bank
 * iii. KiaKia To OtherBanks
 *
 */

public class Model2 extends Model1 {

    public Model2(){}

    public Model2(ReportItem reportItem)
    {
        setAmount("NGN"+ Misc.toMoneyFormat(reportItem.getAmount() / 100.0));
        setID(reportItem.getID() + "");
        setDate(reportItem.getDate());
        setFrom(reportItem.getFrom());
        setFromPhoneNumber(reportItem.getFromPhoneNumber());
        setTo(reportItem.getTo());
    }

    private String Amount;

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }
}
