package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.appzonegroup.app.fasttrack.fragment.online.EnterDetailFragment;
import com.appzonegroup.app.fasttrack.fragment.online.ListOptionsFragment;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.creditclub.core.data.Encryption;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;

import java.util.concurrent.TimeoutException;

public class OnlineActivity extends BaseActivity {
    APIHelper ah;
    BankOneApplication bankOneApplication;
    public static boolean isHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet);
        bankOneApplication = (BankOneApplication)getApplication();
        ah = new APIHelper(getBaseContext());
        isHome = false;
        goHome();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void goHome(){

        if (isHome)
        {
            showCloseDialog();
            return;
        }

        isHome = true;
        //(isHome)
        {
            final ProgressDialog loading = new ProgressDialog(this);
            loading.setMessage("Loading...");
            loading.setCanceledOnTouchOutside(false);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setPositiveButton("OK", null);

            final String phoneNumber = bankOneApplication.getAuthResponse().getPhoneNumber();
            final String verificationCode = bankOneApplication.getAuthResponse().getActivationCode();
            final String sessionId = Encryption.generateSessionId(phoneNumber);
            String finalLocation = "0.00;0.00";
            final GPSTracker gpsTracker = new GPSTracker(this);
            if(gpsTracker.getLocation()!=null){
                String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                finalLocation = latitude+";"+longitude;
            }
            loading.show();
            Misc.resetTransactionMonitorCounter(getBaseContext());
            ah.attemptValidation(phoneNumber, sessionId, verificationCode, finalLocation, false, new APIHelper.VolleyCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result, boolean status) {
                    loading.dismiss();
                    if (e == null && result != null) {
                        try {
                            String answer = Response.fixResponse(result);
                            String decryptedAnswer = Encryption.decrypt(answer);
                            JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
                            if (response == null) {
                                Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                                dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                            } else {
                                Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.SUCCESS_COUNT, sessionId);
                                String resp = response.toString();
                                JSONObject response_base = response.getJSONObject("Response");
                                if (response_base != null) {
                                    int shouldClose = response_base.optInt("ShouldClose", 1);
                                    if (shouldClose == 0) {
                                        /*JSONObject auth = new JSONObject();
                                        auth.put("phone_number", phoneNumber);
                                        auth.put("session_id", sessionId);
                                        new CacheHelper(getApplicationContext()).saveCacheAuth(auth.toString());*/
                                        bankOneApplication.getAuthResponse().setSessionId(sessionId);
                                        if (resp.contains("MenuItem")) {
                                            JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                            getFragmentManager().beginTransaction()
                                                    .replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, true))
                                                    .commitAllowingStateLoss();
                                        } else {
                                            Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                            if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                                JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                                getFragmentManager().beginTransaction()
                                                        .replace(R.id.container, EnterDetailFragment.instantiate(data, resp.contains("ACTIVATION CODE")))
                                                        .commit();
                                            } else {
                                                String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                                dialog.setMessage(Html.fromHtml(message)).show();
                                            }
                                        }
                                    } else {
                                        if (response_base.toString().contains("Display")) {
                                            dialog.setMessage(Html.fromHtml(response_base.getJSONObject("Menu").getJSONObject("Response")
                                                    .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED))).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                                    goHome();
                                                }
                                            })
                                                    .show();
                                        } else {
                                            dialog.setMessage(Html.fromHtml(response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED))).show();
                                        }
                                    }
                                }
                            }
                        } catch (Exception c) {
                            FirebaseCrashlytics.getInstance().recordException(c);
                            c.printStackTrace();
                        }
                    } else {
                        if (e != null) {
                            e.printStackTrace();
                            if (e instanceof TimeoutException) {
                                Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                                dialog.setMessage("Something went wrong! Please try again.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                goHome();
                                            }
                                        }).setCancelable(false)
                                        .show();
                            } else {
                                Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                                dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                            }
                        } else {
                            Misc.increaseTransactionMonitorCounter(getBaseContext(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                            dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                        }
                    }
                }
            }/*, new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    Log.e("FutureCall", result + "");
                }
            }*/);
        }

    }

    @Override
    public void onBackPressed() {

        goHome();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_go_offline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_go_offline) {
            showCloseDialog();
        }

        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return true;
    }

    private void showCloseDialog()
    {
        final Dialog dialog = Dialogs.getQuestionDialog(this, "Go Offline?");
        dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }
}
