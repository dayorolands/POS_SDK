package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.MainMenuItem;

import java.util.ArrayList;

/**
 * Created by Joseph on 12/13/2017.
 */

public class MainMenuAdapter extends ArrayAdapter<MainMenuItem> {

    Activity activity;
    ArrayList<MainMenuItem> mainMenuItems;

    public MainMenuAdapter(@NonNull Activity activity, @NonNull ArrayList<MainMenuItem> objects) {
        super(activity, 0, objects);
        this.activity = activity;
        this.mainMenuItems = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.main_menu_item, parent, false);

        convertView.setBackgroundResource(mainMenuItems.get(position).getImageId());

        return convertView;


    }
}
