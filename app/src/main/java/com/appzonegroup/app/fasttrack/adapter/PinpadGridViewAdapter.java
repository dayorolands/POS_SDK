package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;

import java.util.ArrayList;

/**
 * Created by Joseph on 11/29/2017.
 */

public class PinpadGridViewAdapter extends ArrayAdapter<String> {

    ArrayList<String> text = new ArrayList<>();
    Activity activity;
    public PinpadGridViewAdapter(@NonNull Activity activity, ArrayList<String> resource) {
        super(activity, 0, resource);
        this.activity = activity;
        text.addAll(resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.pinpad_item, parent, false);
        }

        ((TextView)convertView.findViewById(R.id.pin_tv)).setText(text.get(position));

        /*if (position == 9 || position == 11)
            convertView.setVisibility(View.INVISIBLE);*/

        return convertView;
    }
}
