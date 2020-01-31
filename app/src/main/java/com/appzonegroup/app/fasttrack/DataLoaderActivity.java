package com.appzonegroup.app.fasttrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.model.AgentInfo;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.task.GetCallTask;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Locale;

/**
 * Created by Oto-obong on 03/08/2017.
 */

public class DataLoaderActivity extends BaseActivity {

    TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_loader);

        statusTextView = findViewById(R.id.statusTextView);

        String url = String.format(Locale.getDefault(), "%s/CreditClubMiddleWareAPI/CreditClubStatic/GetAgentInfoByPhoneNumber?phoneNumber=%s&institutionCode=%s",
                AppConstants.getBaseUrl(), LocalStorage.getPhoneNumber(this), LocalStorage.getInstitutionCode(this));

        GetCallTask getCallTask = new GetCallTask(getProgressDialog(), this, this);
        getCallTask.execute(url);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getBaseContext(), "Still loading data... Please wait", Toast.LENGTH_LONG).show();
    }

    @Override
    public void processFinished(String output) {
        if (output != null) {
            try {
                AgentInfo info = new Gson().fromJson(output, AgentInfo.class);
                if (info.isStatus()) {
                    LocalStorage.setAgentInfo(this, output);
                    //LocalStorage.SaveValue(AppConstants.AGENT_NAME, info.getAgentName(), this);
                }
                Intent intent = new Intent(DataLoaderActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
                Crashlytics.logException(ex);
                Toast.makeText(getBaseContext(), "No details was received. Try again", Toast.LENGTH_LONG).show();
                if (!isFinishing()) finish();
            }
        }
        /*else
        {
            if (LocalStorage.getAgentInfo(this) == null)
            {
                showError("You do not have internet on your device. Please make internet available and try again");
            }
        }*/
    }
}
