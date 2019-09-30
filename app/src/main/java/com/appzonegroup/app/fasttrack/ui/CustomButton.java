package com.appzonegroup.app.fasttrack.ui;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;

import com.appzonegroup.app.fasttrack.R;

/**
 * Created by madunaguekenedavid on 13/04/2018.
 */


public class CustomButton extends AppCompatButton {

    public CustomButton(Context context) {
        super( context );
        setFont();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super( context, attrs );
        setFont();
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        setFont();
    }

    private void setFont() {
        Typeface normal = Typeface.createFromAsset(getContext().getAssets(),getResources().getString(R.string.font_name));
        setTypeface( normal, Typeface.NORMAL );

        Typeface bold = Typeface.createFromAsset( getContext().getAssets(), getResources().getString(R.string.font_name) );
        setTypeface( normal, Typeface.BOLD );
    }




}
