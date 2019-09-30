package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.LoanRequest;
import com.appzonegroup.app.fasttrack.DetailsActivity;
import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.utility.Functions;
import com.appzonegroup.app.fasttrack.utility.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/14/2016.
 */
public class LoanRequestReportAdapter extends ArrayAdapter<ReportItem> {

    Activity activity;
    ArrayList<ReportItem> reportItems;
    public LoanRequestReportAdapter(Activity activity, ArrayList<ReportItem> reportItems)
    {
        super(activity, 0, reportItems);
        this.activity = activity;
        this.reportItems = reportItems;
    }

    public View getView(int position, View view, ViewGroup parent)
    {

        if (view == null){
            view = activity.getLayoutInflater().inflate(R.layout.deposit_report_item, parent, false);
        }

        ReportItem reportItem = reportItems.get(position);
        ((TextView)view.findViewById(R.id.customer_name_tv)).setText(reportItem.getCustomerName());
        ((TextView)view.findViewById(R.id.phone_number_tv)).setText(reportItem.getCustomerPhone());
        ((TextView)view.findViewById(R.id.amount_tv)).setText(Misc.toMoneyFormat(reportItem.getAmount() / 100.0));
        ((TextView)view.findViewById(R.id.time_tv)).setText(reportItem.getDate().replace('T', ' '));

        return view;
    }

}
