package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.model.report.Model1;
import com.appzonegroup.app.fasttrack.model.report.Model2;
import com.appzonegroup.app.fasttrack.model.report.Model3;
import com.appzonegroup.app.fasttrack.model.report.Model4;
import com.appzonegroup.app.fasttrack.model.report.ReportBaseClass;
import com.appzonegroup.app.fasttrack.utility.Misc;

import java.util.ArrayList;


public class ReportsAdapter extends ArrayAdapter<ReportItem> {

    private Activity activity;
    private ArrayList<ReportItem> reportItems;
    private int reportType;
    public ReportsAdapter(Activity activity, ArrayList<ReportItem> reportItems, int reportType)
    {
        super(activity, 0, reportItems);
        this.activity = activity;
        this.reportItems = reportItems;
        this.reportType = reportType;
    }

    public View getView(int position, View view, ViewGroup parent)
    {

        int viewID = getViewID();

        if (view == null){
            view = activity.getLayoutInflater().inflate(viewID, //R.layout.item_report,
                    parent, false);
        }

        switch (reportType)
        {
            case 1:case 6:
            {
                Model1 reportItem = new Model1(reportItems.get(position));
                ((TextView)view.findViewById(R.id.from_tv)).setText(reportItem.getFrom());
                ((TextView)view.findViewById(R.id.to_tv)).setText(reportItem.getTo());
                if (reportType == 6) {
                    view.findViewById(R.id.to_tv).setVisibility(View.GONE);
                    view.findViewById(R.id.id_label).setVisibility(View.GONE);
                }
                ((TextView)view.findViewById(R.id.date_tv)).setText(reportItem.getDate().replace("T", " "));
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportItem.getFromPhoneNumber());
                break;
            }
            case 2:
            {
                Model2 reportItem = new Model2(reportItems.get(position));
                ((TextView)view.findViewById(R.id.from_tv)).setText(reportItem.getFrom());
                ((TextView)view.findViewById(R.id.amount_tv)).setText(reportItem.getAmount());
                ((TextView)view.findViewById(R.id.date_tv)).setText(reportItem.getDate().replace("T", " "));
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportItem.getFromPhoneNumber());
                break;
            }
            case 3:
            {
                Model4 reportItem = new Model4(reportItems.get(position));
                ((TextView)view.findViewById(R.id.from_tv)).setText(reportItem.getFrom());
                ((TextView)view.findViewById(R.id.product_name_tv)).setText(reportItem.getProductName());
                ((TextView)view.findViewById(R.id.date_tv)).setText(reportItem.getDate().replace("T", " "));
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportItem.getFromPhoneNumber());
                ((TextView)view.findViewById(R.id.customer_phone_no_tv)).setText(reportItem.getCustomerPhone());
                ((TextView)view.findViewById(R.id.customer_name_tv)).setText(reportItem.getCustomerName());
                break;
            }
            case 4:
            {
                Model3 reportItem = new Model3(reportItems.get(position));
                ((TextView)view.findViewById(R.id.from_tv)).setText(reportItem.getFrom());
                ((TextView)view.findViewById(R.id.amount_tv)).setText(reportItem.getAmount());
                ((TextView)view.findViewById(R.id.date_tv)).setText(reportItem.getDate().replace("T", " "));
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportItem.getFromPhoneNumber());
                ((TextView)view.findViewById(R.id.customer_name_tv)).setText(reportItem.getCustomerName());
                break;
            }
            case 5:
            {
                ReportBaseClass reportBaseClass = new ReportBaseClass(reportItems.get(position));
                ((TextView)view.findViewById(R.id.from_tv)).setText(reportBaseClass.getFrom());
                ((TextView)view.findViewById(R.id.id_tv)).setText(reportBaseClass.getID());
                ((TextView)view.findViewById(R.id.date_tv)).setText(reportBaseClass.getDate().replace("T", " "));
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportBaseClass.getFromPhoneNumber());
                break;
            }
            default:{
                ReportItem reportItem = (ReportItem) reportItems.get(position);
                // for the repetitive parameters
                ((TextView)view.findViewById(R.id.time_occurred_tv)).setText(reportItem.getFormatedDate());
                ((TextView)view.findViewById(R.id.customer_name_tv)).setText(reportItem.getCustomerName() == null ? "[No name]" : reportItem.getCustomerName());
                ((TextView)view.findViewById(R.id.phone_no_tv)).setText(reportItem.getCustomerPhone());
                ((TextView)view.findViewById(R.id.amount_tv)).setText("NGN"+Misc.toMoneyFormat(reportItem.getAmount() / 100.0));
            }
        }



        return view;
    }

    private int getViewID()
    {
        switch (reportType)
        {
            case 0:default: return R.layout.item_report;
            case 1:case 6: return R.layout.item_report_1;
            case 2: return R.layout.item_report_2;
            case 3: return R.layout.item_report_4;
            case 4: return R.layout.item_report_3;
            case 5: return R.layout.item_report_5;

        }
    }

}
