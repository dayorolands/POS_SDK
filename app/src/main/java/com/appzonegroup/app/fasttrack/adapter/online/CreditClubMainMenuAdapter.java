package com.appzonegroup.app.fasttrack.adapter.online;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.appzonegroup.app.fasttrack.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Joseph on 3/20/2018.
 */

public class CreditClubMainMenuAdapter extends ArrayAdapter<String> {// RecyclerView.Adapter<CreditClubMainMenuAdapter.GmailVH> {

    List<String> dataList;
    Activity activity;
    ColorGenerator generator = ColorGenerator.MATERIAL;

    /*int colors[] = {R.color.red, R.color.pink, R.color.purple, R.color.deep_purple,
            R.color.indigo, R.color.blue, R.color.light_blue, R.color.cyan, R.color.teal, R.color.green,
            R.color.light_green, R.color.lime, R.color.yellow, R.color.amber, R.color.orange, R.color.deep_orange};*/

    public CreditClubMainMenuAdapter(Activity activity, ArrayList<String> dataList)
    {
        super(activity, 0, dataList);
        this.activity = activity;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = activity.getLayoutInflater().inflate(R.layout.credit_club_menu_list_item, parent, false);
            /*if (position%2 == 0)
                convertView = activity.getLayoutInflater().inflate(R.layout.credit_club_menu_list_item, parent, false);
            else
                convertView = activity.getLayoutInflater().inflate(R.layout.creditclub_menu_list_item_right, parent, false);*/
        }
        String letter = String.format(Locale.getDefault(), "%d.", (position + 1));
        TextDrawable drawable = TextDrawable.builder().buildRound(letter, generator.getRandomColor());
        ((ImageView)convertView.findViewById(R.id.gmail_item_letter)).setImageDrawable(drawable);
        ((TextView)convertView.findViewById(R.id.gmail_item_title)).setText(dataList.get(position));

        return convertView;
    }

    /*@Override
    public GmailVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.credit_club_menu_list_item, viewGroup, false);
        return new GmailVH(view);
    }*/

    /*@Override
    public void onBindViewHolder(GmailVH gmailVH, int i) {
        gmailVH.title.setText(dataList.get(i));
//        Get the first letter of list item
        letter = String.format(Locale.getDefault(), "%d.", (i + 1));//"" String.valueOf(dataList.get(i).charAt(0));

//        Create a new TextDrawable for our image's background
        TextDrawable drawable = TextDrawable.builder().buildRound(letter, generator.getRandomColor());

        gmailVH.letter.setImageDrawable(drawable);
    }*/

    /*@Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }*/
}