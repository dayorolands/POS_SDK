package com.appzonegroup.app.fasttrack.adapter.online;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.online.Option;

import java.util.ArrayList;

/**
 * Created by fdamilola on 8/13/15.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {

    Context ctx;
    int res;
    ArrayList<Option> data;

    public OptionsAdapter(Context context, int resource, ArrayList<Option> objects) {
        super(context, resource, objects);
        this.ctx = context;
        this.res = resource;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null){
            v = LayoutInflater.from(this.ctx).inflate(this.res, parent, false);
        }

        ViewHolder vh = new ViewHolder(v);
        Option option = getItem(position);
        vh.optionName.setText(option.getName());

        return v;
    }

    @Override
    public Option getItem(int position) {
        return this.data.get(position);
    }

    private class ViewHolder {
        TextView optionName;
        public ViewHolder(View v){
            optionName = v.findViewById(R.id.optionName);
        }
    }
}
