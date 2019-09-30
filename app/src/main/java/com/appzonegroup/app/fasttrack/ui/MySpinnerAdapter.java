package com.appzonegroup.app.fasttrack.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.appzonegroup.app.fasttrack.R;

import java.util.List;

/**
 * Created by madunaguekenedavid on 16/04/2018.
 */

public class MySpinnerAdapter<String> extends ArrayAdapter<String> {
    // Initialise custom font, for example:
    Typeface font = Typeface.createFromAsset(getContext().getAssets(),
            getContext().getResources().getString(R.string.font_name));

    // (In reality I used a manager which caches the Typeface objects)
    // Typeface font = FontManager.getInstance().getFont(getContext(), BLAMBOT);

    public MySpinnerAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
    }

    // Affects default (closed) state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        androidx.appcompat.widget.AppCompatTextView view = (androidx.appcompat.widget.AppCompatTextView) super.getView(position, convertView, parent);
        view.setTypeface(font);
        return view;
    }

    // Affects opened state of the spinner
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        androidx.appcompat.widget.AppCompatCheckedTextView view = (androidx.appcompat.widget.AppCompatCheckedTextView) super.getDropDownView(position, convertView, parent);
        view.setTypeface(font);
        return view;
    }
}