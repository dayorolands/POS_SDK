package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.BillCategory;
import com.appzonegroup.app.fasttrack.model.Biller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oto-obong on 14/08/2017.
 */

public class BillerAdapter extends ArrayAdapter<Biller> {

    Activity activity;
    List<Biller> billers;

    public BillerAdapter(Activity activity, List<Biller> billers) {
        super(activity, 0, billers);
        this.activity = activity;
        this.billers = billers;
    }

    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = activity.getLayoutInflater().inflate(R.layout.biller_report_item, parent, false);
        }

        Biller biller = billers.get(position);
        ((TextView) view.findViewById(R.id.biller_name_tv)).setText(biller.getBillerNameField());

        return view;
    }
}


