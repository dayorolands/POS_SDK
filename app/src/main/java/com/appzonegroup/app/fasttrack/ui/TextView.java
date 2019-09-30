package com.appzonegroup.app.fasttrack.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.appzonegroup.app.fasttrack.R;

/**
 * TextView with options for setting custom font
 */
public class TextView extends AppCompatTextView {

    private static final String TAG = "TextView";

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomTextView);

        a.getString(R.styleable.CustomTextView_my_font);
        String customFontSize = a.getString(R.styleable.CustomTextView_fontSize);
        String customStyle = "3";//a.getString(R.styleable.CustomTextView_fontStyle);

        if (customStyle != null){
            setTextStyle(ctx, customStyle);
        }

        if (a.getString(R.styleable.CustomTextView_my_font) != null)
        {
            setCustomFont(ctx, a.getString(R.styleable.CustomTextView_my_font), customStyle);
        }else{
            setCustomFont(ctx, getResources().getString(R.string.font_name), customStyle);
        }

        if (a.getString(R.styleable.CustomTextView_fontColor) != null)
        {
            //String customColor = a.getString(R.styleable.CustomTextView_fontColor);
            setColor(ctx, a.getString(R.styleable.CustomTextView_fontColor));
        }
        if (customFontSize != null)
        {
            float fontsize = Float.parseFloat(customFontSize.substring(0, 2));
            setFontSize(fontsize);
        }


        a.recycle();
    }

    void setTextStyle(Context context, String appearance)
    {

    }

    private void setColor(Context context, String color)
    {
        setTextColor(context.getResources().getColor(R.color.black));
    }

    private void setFontSize(float size)
    {
        setTextSize(size);
    }

    public boolean setCustomFont(Context ctx, String asset, String customStyle) {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(ctx.getAssets(), asset);

        } catch (Exception e) {
            //tf = Typeface.createFromAsset(ctx.getAssets(), getResources().getString(R.string.font_name));
            //Log.e(TAG, "Could not get typeface: "+e.getMessage());
            //return false;
        }
        if (customStyle == null)
            setTypeface(tf);
        else{
            if (customStyle.equals("0")){
                setTypeface(tf, Typeface.BOLD);
            }
            else if (customStyle.equals("1")){
                setTypeface(tf, Typeface.ITALIC);
            }
            else if (customStyle.equals("2")){
                setTypeface(tf, Typeface.BOLD_ITALIC);
            }else{
                setTypeface(tf, Typeface.NORMAL);
            }
        }

        return true;
    }


}
