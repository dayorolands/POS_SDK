package com.appzonegroup.app.fasttrack.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.appzonegroup.app.fasttrack.OpenAccountActivity;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.utility.ImageManipulations;

/**
 * Created by Oto-obong on 13/7/2017.
 */

public class IdentityPhotoFragment extends Fragment {

    public IdentityPhotoFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_agent_photo, container, false);

        OpenAccountActivity.identityPhoto_ImageView = (ImageView)rootView.findViewById(R.id.capturePhoto_agent_photo_imageView);

        if (OpenAccountActivity.account.getID() != 0){
            OpenAccountActivity.identityPhoto_ImageView.setImageBitmap(ImageManipulations.StringToBitmap(OpenAccountActivity.account.getSecondaryIdentityInBytes()));
        }




        return rootView;
    }
}
