package com.appzonegroup.app.fasttrack.model.report;

import com.appzonegroup.app.fasttrack.model.ReportItem;

/**
 * This is the model class for the following reports:
 * i. Balance Enquiry
 * ii. Mini statement
 * iii. Recharge
 * iv. Kiakia to kiakia
 */

public class Model1 extends ReportBaseClass {

    public Model1(){}

    public Model1(ReportItem reportItem)
    {
        setID(reportItem.getID() + "");
        setDate(reportItem.getDate());
        setFrom(reportItem.getFrom());
        setFromPhoneNumber(reportItem.getFromPhoneNumber());
        setTo(reportItem.getTo());
    }

    private String To;

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }
}
