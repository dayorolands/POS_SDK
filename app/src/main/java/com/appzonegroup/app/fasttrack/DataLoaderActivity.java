package com.appzonegroup.app.fasttrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.model.AgentInfo;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Token;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.TripleDES;
import com.appzonegroup.app.fasttrack.utility.task.GetCallTask;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Locale;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Oto-obong on 03/08/2017.
 */

public class DataLoaderActivity extends BaseActivity {

    TextView statusTextView;
    //String customerResult = "";
    Token token;
    /*static Boolean ok = false;
    static Boolean confirmed;*/
    Boolean activateDecryption = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loader);

        statusTextView = findViewById(R.id.statusTextView);

        String url = String.format(Locale.getDefault(), "%s/CreditClubMiddleWareAPI/CreditClubStatic/GetAgentInfoByPhoneNumber?phoneNumber=%s&institutionCode=%s",
                AppConstants.getBaseUrl(), LocalStorage.getPhoneNumber(this), LocalStorage.getInstitutionCode(this));

        GetCallTask getCallTask = new GetCallTask(getProgressDialog(), this, this);
        getCallTask.execute(url);

        loadAllOtherData();

    }

    private void loadAllOtherData() {

        statusTextView.setText("Fetching Token");
        getTokenKey();
    }

    private void getTokenKey() {

            new Thread() {
                public void run() {

                    String tokenUrl = AppConstants.getApiTokenUrl() + "GetToken?appId=edef4ef";
                    Observable<String> observable = Observable.from(new String[]{APICaller.makeGetRequest2(tokenUrl)});
                    observable.subscribe(new Action1<String>() {

                        @Override
                        public void call(String s) {


                            if (s != null) {

                                activateDecryption = true;
                                token = new Gson().fromJson(s.trim(), Token.class);

                                try {

                                    String Token =  TripleDES.decrypt(token.getToken());
                                    LocalStorage.SaveValue(AppConstants.API_TOKEN, Token, getBaseContext());
                                    Intent intent = new Intent(DataLoaderActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                } catch (Exception e) {
                                    e.printStackTrace();

                                    Crashlytics.logException(new Exception(e.getMessage()));
                                }

                            } else {
                                showNoDetailNotification();
                            }

                        }
                    });

                }

            }.start();

        }

    void showNoDetailNotification(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "No details was received. Try again", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Toast.makeText(getBaseContext(), "Still loading data... Please wait", Toast.LENGTH_LONG).show();
    }

    @Override
    public void processFinished(String output) {
        if (output != null)
        {
            AgentInfo info = new Gson().fromJson(output, AgentInfo.class);
            if (info.isStatus())
            {
                LocalStorage.setAgentInfo(this, output);
                //LocalStorage.SaveValue(AppConstants.AGENT_NAME, info.getAgentName(), this);
            }
        }
        /*else
        {
            if (LocalStorage.getAgentInfo(this) == null)
            {
                showError("You do not have internet on your device. Please make interrnet available and try again");
            }
        }*/
    }
}
