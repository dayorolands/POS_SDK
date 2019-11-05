package com.appzonegroup.creditclub.pos.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.appzonegroup.creditclub.pos.service.ApiService;

/*import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;*/

/**
 * Created by Joseph on 8/22/2018.
 */

public class UpdateChecker implements Runnable {

    Context context;
    private String TAG = this.getClass().getSimpleName();

    public UpdateChecker(Context context, String threadInfo) {
        this.context = context;
        Log.e("TAG", "Update checker constructor--" + threadInfo);
    }

    @Override
    public void run() {
        String latestVersion = getAppLatestVersion();

        processVersion(latestVersion);
    }

    private void processVersion(String latestVersion) {
        if (latestVersion != null) {
            try {
                long currentTime = Misc.getCurrentDateTime().getTime();
                if (!getCurrentVersion().equals(latestVersion)) {
                    //if (!isFinishing()) No longer a concern
                    { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error

//                        long //timeDifference = 0,
//                                updateCheckDateTime = 0;
//                        try {
//                            String checkTime = LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_DATE, context);
//                            updateCheckDateTime = Long.parseLong(checkTime);
//                        } catch (Exception ex) {
//                            updateCheckDateTime = Misc.getCurrentDateTime().getTime();
//                            LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_DATE, String.valueOf(currentTime), context);
//                            ex.printStackTrace();
//                        }
//
//                        long timeDifference = Math.abs(currentTime - updateCheckDateTime);
//                        LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(timeDifference), context);
//
//                        if (timeDifference > 16416000)//432000000
//                        {
//                            Log.e(TAG, "Show notification");
//                            Notification.showNotification(context, "Pending Update",
//                                    "You have less than " + (int) (timeDifference / 1000 / 3600 / 24) + " day(s) to update your app");
//                        } else {
//                            Log.e(TAG, "Don't show notification");
//                        }
                        //updateChecked = true;
                    }
                } else {
                    Log.e(TAG, "Up to date");
//                    LocalStorage.remove(AppConstants.UPDATE_CHECK_DATE, context);
//                    LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(0), context);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getAppLatestVersion() {
        String latestVersion = null;
        try {
            String url = ApiService.BASE_URL + "/CreditClubMiddleWareAPI/api/Version/GetLatestVersion";
            latestVersion = ApiService.INSTANCE.get(url).getValue();

            if (latestVersion != null)
                latestVersion = latestVersion.replace("\"", "");

            //It retrieves the latest version by scraping the content of current version from play store at runtime
            /*Document doc = Jsoup.connect("https://play.google.com/store/apps/details?" +
                    "id=com.appzone.android.bankonemobile.creditclub").get();
            latestVersion = doc.getElementsByClass("htlgb").get(6).text();*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, latestVersion + "");
        return latestVersion;
    }

    private String getCurrentVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e(TAG, "Current version" + pInfo.versionName);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
