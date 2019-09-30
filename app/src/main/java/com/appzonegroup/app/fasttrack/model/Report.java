package com.appzonegroup.app.fasttrack.model;

import java.util.ArrayList;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Report {



    private ArrayList<ReportItem> Reports;
    private int totalCount;

    public ArrayList<ReportItem> getReports() {
        return Reports;
    }

    public void setReports(ArrayList<ReportItem> reports) {
        Reports = reports;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
