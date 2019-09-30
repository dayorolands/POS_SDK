package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.Beneficiary;
import com.appzonegroup.app.fasttrack.model.BillCategory;
import com.appzonegroup.app.fasttrack.model.Biller;
import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.utility.Misc;

import java.util.ArrayList;

/**
 * Created by Oto-obong on 21/08/2017.
 */

public class BeneficiaryAdapter extends ArrayAdapter<Beneficiary> {

    Activity activity;
    ArrayList<Beneficiary> beneficiaries;
    ArrayList<Beneficiary> originalData;
    public BeneficiaryAdapter(Activity activity, ArrayList<Beneficiary> beneficiaries)
    {
        super(activity, 0, beneficiaries);
        this.activity = activity;
        this.beneficiaries = beneficiaries;
        this.originalData = new ArrayList<>(this.beneficiaries);
    }

    public View getView(int position, View view, ViewGroup parent)
    {

        if (view == null){
            view = activity.getLayoutInflater().inflate(R.layout.beneficiary_report_item, parent, false);
        }

        Beneficiary beneficiary = beneficiaries.get(position);
        ((TextView)view.findViewById(R.id.beneficiary_firstName_tv)).setText(beneficiary.getFirstName());
        ((TextView)view.findViewById(R.id.beneficiary_lastName_tv)).setText(beneficiary.getLastName());
        ((TextView)view.findViewById(R.id.beneficiary_accountNumber_tv)).setText(beneficiary.getAccountNumber());
        return view;
    }

    @Override
    public Filter getFilter(){
        return new Filter(){

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                Filter.FilterResults result = new FilterResults();

                if (constraint != null && constraint.toString().trim().length() > 0) {
                    ArrayList<Beneficiary> founded = new ArrayList<>();

                    for(Beneficiary item: beneficiaries){

                        if(item.getFirstName().toString().toLowerCase().contains(constraint) || item.getLastName().toString().toLowerCase().contains(constraint)){

                            founded.add(item);
                        }
                    }

                    result.values = founded;

                    result.count = founded.size();
                }else {
                    result.values = originalData;

                    result.count = originalData.size();
                }
                return result;


            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                for (Beneficiary item : (ArrayList<Beneficiary>) results.values) {

                    add(item);
                }
                notifyDataSetChanged();

            }

        };
    }


}
