package com.appzonegroup.creditclub.pos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.appzonegroup.creditclub.pos.service.SyncService;

/**
 * Created by Joseph on 6/5/2016.
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, SyncService.class);

        try {
            context.startService(i);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();

            i.putExtra("NEED_FOREGROUND_KEY", true);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
            } catch (IllegalStateException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
