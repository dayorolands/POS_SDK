package com.appzonegroup.app.fasttrack.adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.MainMenuItem;

import java.util.ArrayList;

/**
 * Created by Joseph on 12/13/2017.
 */

public class CreditClubMainMenuAdapter extends ArrayAdapter<MainMenuItem> {

    Activity activity;
    ArrayList<MainMenuItem> mainMenuItems;

    public CreditClubMainMenuAdapter(@NonNull Activity activity, @NonNull ArrayList<MainMenuItem> objects) {
        super(activity, 0, objects);
        this.activity = activity;
        this.mainMenuItems = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.creditclub_main_menu_item, parent, false);

        ((ImageView)convertView.findViewById(R.id.menu_bg_image_view)).setImageResource(mainMenuItems.get(position).getImageId());
        ((TextView)convertView.findViewById(R.id.menu_label_tv)).setText(mainMenuItems.get(position).getText());

        return convertView;


    }
}
