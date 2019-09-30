package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import com.appzonegroup.app.fasttrack.R;

/**
 * Created by Joseph on 5/27/2016.
 */
public class CalendarDialog {

    private static Dialog getCalendarDialog(Activity activity)
    {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.calendar_dialog);

        return dialog;
    }

    public static Dialog showCalendarDialog(Activity activity)
    {
        Dialog dialog = getCalendarDialog(activity);
        dialog.show();
        return  dialog;
    }
}
