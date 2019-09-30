package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.BillerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oto-obong on 15/08/2017.
 */

public class BillerItemAdapter extends ArrayAdapter<BillerItem> {

        Activity activity;
        List<BillerItem> billerItems;

    public BillerItemAdapter(Activity activity, List<BillerItem> billerItems)
    {
        super(activity, 0, billerItems);
        this.activity = activity;
        this.billerItems = billerItems;
    }

    public View getView(int position, View view, ViewGroup parent)
    {

        if (view == null){
        view = activity.getLayoutInflater().inflate(R.layout.billeritem_report_item, parent, false);
        }

        BillerItem billerItem = billerItems.get(position);
        ((TextView)view.findViewById(R.id.billeritem_name_tv)).setText(billerItem.getBillerItemNameField());

        return view;
    }
}


