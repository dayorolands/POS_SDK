package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.BillCategory;

import java.util.ArrayList;

/**
 * Created by Oto-obong on 01/08/2017.
 */

public class CategoryAdapter  extends ArrayAdapter<BillCategory> {

    Activity activity;
    ArrayList<BillCategory> billCategories;
    ArrayList<BillCategory> originalData;
    public CategoryAdapter(Activity activity, ArrayList<BillCategory> billCategories)
    {
        super(activity, 0, billCategories);
        this.activity = activity;
        this.billCategories = billCategories;
        this.originalData = new ArrayList<BillCategory>(this.billCategories);

    }

    public View getView(int position, View view, ViewGroup parent)
    {

        if (view == null){
            view = activity.getLayoutInflater().inflate(R.layout.category_report_item, parent, false);
        }

        BillCategory billCategory = billCategories.get(position);
        ((TextView)view.findViewById(R.id.category_name_tv)).setText(billCategory.getName());
        ((TextView)view.findViewById(R.id.category_desc_tv)).setText(billCategory.getDescription());

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
                    ArrayList<BillCategory> founded = new ArrayList<>();

                    for(BillCategory item: billCategories){

                        if(item.getName().toString().toLowerCase().contains(constraint)){

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
                for (BillCategory item : (ArrayList<BillCategory>) results.values) {

                    add(item);
                }
                notifyDataSetChanged();

            }

        };
    }

}

