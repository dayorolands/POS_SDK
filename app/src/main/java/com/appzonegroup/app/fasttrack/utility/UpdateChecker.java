package com.appzonegroup.app.fasttrack.utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.utility.task.AsyncResponse;
import com.appzonegroup.app.fasttrack.utility.task.GetCallTask;

import java.util.Locale;

/**
 * Created by Joseph on 8/22/2018.
 */

public class UpdateChecker implements AsyncResponse// implements Runnable
{

    Context context;
    private String TAG = this.getClass().getSimpleName();
    UpdateChecker(Context context, String threadInfo)
    {
        this.context = context;
        Log.e("TAG", "Update checker constructor--" + threadInfo);
    }



    //@Override
    public void run() {

        GetCallTask callTask = new GetCallTask(null, this);
        String url = String.format(Locale.getDefault(), "%s/CreditClubMiddleWareAPI/api/Version/GetLatestVersion?appName=CreditClubPlus",
                AppConstants.getBaseUrl());

        callTask.execute(url);

        /*String latestVersion = getAppLatestVersion();

        processVersion(latestVersion);*/
    }

    private void processVersion(String latestVersion)
    {
        if(latestVersion != null) {
            try {
                long currentTime = Misc.getCurrentDateTime().getTime();
                String currentVersion = getCurrentVersion();
                Log.e("UpdateChecker", String.format(Locale.getDefault(), "Current version: '%s' ::: Latest Version: '%s' ::: Equal: %s",
                        currentVersion, latestVersion, String.valueOf(currentVersion.equals(latestVersion))));

                if (!getCurrentVersion().equals(latestVersion)) {
                    //if (!isFinishing()) No longer a concern
                    { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error

                        long //timeDifference = 0,
                                updateCheckDateTime = 0;
                        try{
                            String checkTime = LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_DATE,context);
                            updateCheckDateTime = Long.parseLong(checkTime);
                        }catch (Exception ex)
                        {
                            updateCheckDateTime = Misc.getCurrentDateTime().getTime();
                            LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_DATE, String.valueOf(currentTime), context);
                            ex.printStackTrace();
                        }

                        long timeDifference = Math.abs(currentTime - updateCheckDateTime);
                        LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(timeDifference), context);

                        if (timeDifference > 16416000)//432000000
                        {
                            Log.e(TAG, "Show notification");
                            Notification.showNotification(context, "Pending Update",
                                    "You have less than " + (int)(timeDifference/1000/3600/24) + " day(s) to update your app");
                        }else
                        {
                            Log.e(TAG, "Don't show notification");
                        }
                        //updateChecked = true;
                    }
                }
                else{
                    Log.e(TAG, "Up to date");
                    LocalStorage.remove(AppConstants.UPDATE_CHECK_DATE, context);
                    LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(0), context);
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /*private String getAppLatestVersion() {

        GetCallTask callTask = new GetCallTask(null, this);
        String url = String.format(Locale.getDefault(), "%s/api/CreditClubMiddleWareAPI/api/Version/GetLatestVersion?appName=CreditClubPlus",
                AppConstants.getBaseUrl());

        callTask.execute(url);

        String latestVersion = null;
        try {
            //It retrieves the latest version by scraping the content of current version from play store at runtime
            Document doc = Jsoup.connect("https://play.google.com/store/apps/details?" +
                    "id=" + context.getPackageName()).get();
                    //"id=com.appzone.android.bankonemobile.creditclub").get();
            latestVersion = doc.getElementsByClass("htlgb").get(6).text();

        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e(TAG, latestVersion + "");
        return latestVersion;
    }*/



    private String getCurrentVersion()
    {
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

    @Override
    public void processFinished(String latestVersion) {
        if (latestVersion == null)
            return;

        Log.e("UpdateChecker", latestVersion);

        latestVersion = latestVersion.replace("\"", "");

        processVersion(latestVersion);
    }
}
