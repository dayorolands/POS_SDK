package com.appzonegroup.app.fasttrack;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;

import java.util.Timer;
import java.util.TimerTask;

public class LocationChangedService extends Service {

    private GPSTracker gpsTracker;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    String latitude;
    String longitude;
    //CacheHelper cah;

    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase.setAndroidContext(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        try {
            gpsTracker = new GPSTracker(getApplicationContext());
            //latitude = gpsTracker.getLocation().getLatitude() + "";
            //longitude = gpsTracker.getLocation().getLongitude() + "";

            //if (LocalStorage.isLoggedIn(getBaseContext()))
            {
                TimerTask timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    latitude = gpsTracker.getLocation().getLatitude() + "";
                                    longitude = gpsTracker.getLocation().getLongitude() + "";
                                } catch (Exception m) {
                                    latitude = "0.0";
                                    longitude = "0.0";
                                }
                                APIHelper luh = new APIHelper(getApplicationContext());
                                BankOneApplication bankOneApplication = (BankOneApplication)getApplication();
                                AuthResponse ar = bankOneApplication.getAuthResponse();
                                if (ar != null && luh != null) {
                                    luh.updateLocationAgent(ar.getPhoneNumber().replace("234", "0"), longitude, latitude, new APIHelper.VolleyCallback<String>() {
                                        @Override
                                        public void onCompleted(Exception e, String result,boolean status) {
                                            if (status) {
                                                if (result.trim().length() > 0) {
                                                    try {
                                                        String encrypted = Response.fixResponse(result, null);
                                                    }catch(Exception c){
                                                        c.printStackTrace();
                                                    }
                                                } else {

                                                }
                                            } else {
                                                if (e != null) {
                                                    e.printStackTrace();
                                                } else {

                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });


                    }
                };
                timer.schedule(timerTask, 0, 60000);
            }
            /*else {

            }*/
        } catch (Exception n) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ensureLocationEnabled();
                }
            });
        }

    }

    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }


    protected void ensureLocationEnabled() {
        int locationMode = 0; // 0 == Settings.Secure.LOCATION_MODE_OFF
        String locationProviders = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                //Log.e("Settings", "Location setting not found", e);
            }
        } else {
            locationProviders = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        }

        boolean locationEnabled = !TextUtils.isEmpty(locationProviders) || (locationMode != 0);
        if (!locationEnabled) {
            Intent s = new Intent(getApplicationContext(), LocationCheckDialog.class);
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(s);
        }
    }


}
