package com.appzonegroup.app.fasttrack.model.report;

import com.appzonegroup.app.fasttrack.model.ReportItem;

/**This class is the report model base class. It is however
 * the model class for these reports:
 * i. PIN change;
 * ii. PIN Reset
 * Created by Joseph on 9/7/2018.
 */

public class ReportBaseClass {

    private String ID;
    private String Date;
    private String From;
    private String FromPhoneNumber;

    public ReportBaseClass(){}

    public ReportBaseClass(ReportItem reportItem)
    {
        setID(String.valueOf(reportItem.getID()));
        setDate(reportItem.getDate());
        setFrom(reportItem.getFrom());
        setFromPhoneNumber(reportItem.getFromPhoneNumber());
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getFromPhoneNumber() {
        return FromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        FromPhoneNumber = fromPhoneNumber;
    }
}
