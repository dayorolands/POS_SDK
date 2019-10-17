package com.appzonegroup.app.fasttrack.model.report;

import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.utility.Misc;

/**
 * Model class for:
 * i. Bills Payment report
 * ii. Cash In report
 * iii. Cash Out report
 */

public class Model3 extends Model4 {

    public Model3() {
    }

    public Model3(ReportItem reportItem) {
        setID(reportItem.getID() + "");
        setDate(reportItem.getDate());
        setFrom(reportItem.getFrom());
        setFromPhoneNumber(reportItem.getFromPhoneNumber());
        setTo(reportItem.getTo());
        setAmount("NGN" + Misc.toMoneyFormat(reportItem.getAmount() / 100.0));
        setProductName(reportItem.getProductName());
        setCustomerName(reportItem.getCustomerName());
        setCustomerID(reportItem.getCustomerID());
    }

    private String Amount;
    private String CustomerID;

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }
}
