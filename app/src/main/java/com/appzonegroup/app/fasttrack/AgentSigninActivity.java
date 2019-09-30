package com.appzonegroup.app.fasttrack;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.LoadDataType;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by DELL on 3/27/2017.
 */

public class AgentSigninActivity extends AppCompatActivity {
    ArrayList<String> institutionCodes, institutionNames;


    Spinner spinner;
    String institutionCode = "";
    String phoneNumber = "";
    String pin = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
    }


    public void signin(View view){





        institutionCode = "000000";
                //"100636";
        phoneNumber = ((EditText)findViewById(R.id.signin_agentActivation_phoneNumberEt)).getText().toString().trim();
        pin = ((EditText)findViewById(R.id.signin_agentActivation_PINEt)).getText().toString().trim();


        if (phoneNumber.length() == 0){
            Toast.makeText(getBaseContext(), "You did not enter your phone number", Toast.LENGTH_LONG).show();
            return;
        }

        if (phoneNumber.length() != 11){
            Toast.makeText(getBaseContext(), "Phone number is incorrect", Toast.LENGTH_LONG).show();
            return;
        }

        if (pin.length() == 0){
            Toast.makeText(getBaseContext(), "You did not enter your pin", Toast.LENGTH_LONG).show();
            return;
        }



        final ProgressDialog progressDialog = new ProgressDialog(AgentSigninActivity.this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Processing...");
        progressDialog.show();

        VerifySignin verifySignin = new VerifySignin(AgentSigninActivity.this,progressDialog);
        verifySignin.execute();

    }

    public class VerifySignin extends AsyncTask<String,String,Boolean>{

        Context context;
        ProgressDialog progressDialog;
        Gson gson = new Gson();

        public VerifySignin(Context context, ProgressDialog progressDialog){

            this.context = context;
            this.progressDialog = progressDialog;

        }

        @Override
        protected Boolean doInBackground(String... params) {

            String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
            String urlString = AppConstants.getBaseUrl()+ "Get/ConfirmPin?agentPhoneNumber=" + phoneNumber + "&institutionCode=" + institutionCode + "&PIN=" + pin;

            String response = APICaller.makeGetRequest(urlString,Token);

            try {


                if (response == null){

                    return false;
                }else if(response.equals("true\n")){

                    return true;

                }else{
                    return false;
                }

            }catch (Exception ex){

                return null;
            }

        }

        @Override
        protected void onPostExecute(Boolean boolresponse) {

            if(boolresponse == true){
                LocalStorage.SaveValue(AppConstants.AGENT_PIN, ((EditText)findViewById(R.id.signin_agentActivation_PINEt)).getText().toString(), getBaseContext());
                LocalStorage.SaveValue(AppConstants.INSTITUTION_CODE, institutionCode, getBaseContext());
                LocalStorage.SaveValue(AppConstants.ACTIVATED, AppConstants.ACTIVATED, getBaseContext());
                LocalStorage.SaveValue(AppConstants.AGENT_PHONE, phoneNumber, getBaseContext());
                progressDialog.dismiss();

                Intent intent = new Intent(AgentSigninActivity.this, DataLoaderActivity.class);
                intent.putExtra(AppConstants.LOAD_DATA, LoadDataType.OTHER_DATA.ordinal());
                startActivity(intent);


            }else {
            progressDialog.dismiss();
            Toast.makeText(getBaseContext(), "Agent's Pin or Phone Number not found", Toast.LENGTH_LONG).show();

            }


        }
    }
}
