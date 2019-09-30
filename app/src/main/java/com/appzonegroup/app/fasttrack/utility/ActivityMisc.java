package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Joseph on 3/16/2018.
 */

public class ActivityMisc {
    public static void startActivity(Activity activity, Class classToStart) {
        activity.startActivity(new Intent(activity, classToStart));

    }
}
