package com.appzonegroup.app.fasttrack.utility;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by DELL on 4/19/2017.
 */

public class CustomAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {


    public static final int INVALID_POSITION = -1;
    private ArrayList<String> resultList;
    String[] Array;


    public CustomAutoCompleteAdapter(Context context, int textViewResourceId, String[] Array) {
        super(context, textViewResourceId, Array);
        this.Array = Array;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }



    @Override
    public int getPosition(String item) {

        int position = 0;
        int temp_position = 0;

       for (String string : Array){
           if(string.equals(item)){
               position = temp_position;
               break;

           }else{

               temp_position++;
           }
       }
     if(position >= 0){

         return position;

        }else{

    return -1;
}


    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    ArrayList<String> matchList = new ArrayList<String>();
                    for (int i = 0; i < Array.length; i++) {
                        if (Array[i].toLowerCase().contains(constraint)) {
                            matchList.add(Array[i]);
                        }
                    }
                    resultList = matchList;
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;

    }
}




