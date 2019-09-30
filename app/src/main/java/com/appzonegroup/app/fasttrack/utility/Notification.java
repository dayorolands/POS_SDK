package com.appzonegroup.app.fasttrack.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.appzonegroup.app.fasttrack.NotificationReceiver;
import com.appzonegroup.app.fasttrack.R;

/**
 * Created by Joseph on 8/22/2018.
 */

public class Notification {



    private final static String CHANNEL_ID = "CC_Channel";

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Credit Club";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String content)
    {
        createNotificationChannel(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content.length() > 40 ? content.substring(0, 40) : content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("Update Now");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.addAction(android.R.drawable.stat_sys_download, "Update", pendingIntent);

        mBuilder.build();

        Log.e("Notification", "Showing notification");

    }
}
