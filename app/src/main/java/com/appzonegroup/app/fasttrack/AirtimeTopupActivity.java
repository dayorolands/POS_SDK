package com.appzonegroup.app.fasttrack;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

/**
 * Created by Oto-obong on 11/7/2017.
 */

public class AirtimeTopupActivity extends AppCompatActivity {

    Spinner airtimeOperatorSpinner;
    EditText phoneNumber_et, amount_et, agentPin_et;
    String networkOperator, phoneNumber, amount, agentPin;
    private ViewPager mViewPager;
    Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airtime_topup);

        airtimeOperatorSpinner = (Spinner) findViewById(R.id.airtime_networkOperator);
        phoneNumber_et = (EditText) findViewById(R.id.airtime_phoneNumber);
        amount_et = (EditText) findViewById(R.id.airtime_amount);
        agentPin_et = (EditText) findViewById(R.id.airtime_agentPin);
    }

    void showNotification(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    void indicateError(String message, View view){

        showNotification(message);
        if (view != null){
            view.requestFocus();
        }
    }


    public void getAirtime(View view){
           networkOperator = airtimeOperatorSpinner.getSelectedItem().toString();
        if (airtimeOperatorSpinner.getSelectedItemPosition() == 0){
            indicateError("No Network Operator was selected", airtimeOperatorSpinner);
            return;
        }

           phoneNumber = phoneNumber_et.getText().toString();
        if (phoneNumber.length() != 11){
            indicateError("Please enter a valid number number", phoneNumber_et);
            return;
        }

           amount = amount_et.getText().toString();
        if (amount == ""){
            indicateError("Please enter an Amount", amount_et);
            return;
        }

           agentPin = agentPin_et.getText().toString();
        if (agentPin == ""){
            indicateError("Please enter your PIN", agentPin_et);
            return;
        }


        /*SyncAirtimeTopup syncAirtimeTopup = new SyncAirtimeTopup(AirtimeTopupActivity.this, tempaccount, gson);
        syncaccount.execute();*/

    }




    private class SyncAirtimeTopup extends AsyncTask<String,String, String> {

        Context context;
        Gson gson;

        public SyncAirtimeTopup(Context context,Gson gson){

            this.context = context;
            this.gson = gson;


        }

        @Override
        protected void onPreExecute() {

            gson = new Gson();

        }

        @Override
        protected String doInBackground(String... params) {




           /* String url = AppConstants.getBaseUrl() + "Sync/RegisterNewCustomer?" +
                    "uniqueReferenceID=" + customerRequest.getReference() + "&" +
                    "institutionCode=" + institutionCode + "&" +
                    "agentPhoneNumber=" + agentPhone + "&" +
                    "agentPIN=" + agentPIN + "&" +
                    "isRetrial=false";



            String customerJson = gson.toJson(customer);

            String response = APICaller.makePostRequest(url, customerJson);
            //Log.e("Sync Ac", response);


            try {

                ModelResponse serverResponse = gson.fromJson(response, ModelResponse.class);

                if (serverResponse.isSussessful() || serverResponse.getReponseMessage().toLowerCase().contains("already been issued")) {
                    customerRequest.setProcessed(true);
                    customerRequest.setDateSync(Functions.getCurrentDateString());
                    doNotification("Sync successful for customerRequest: " + customerRequest.getFirstName() + " " + customerRequest.getLastName());
                    long id = accountDAO.UpdateAccount(customerRequest);
                    Log.e("Update customerRequest", "Update result: " + id);
                    return "Account has been processed";

                } else {
                    customerRequest.setValidationRemark(serverResponse.getReponseMessage());
                    return serverResponse.getReponseMessage();
                }




            }catch (Exception ex){
                return null;

            }
*/         return "true";
        }

        @Override
        protected void onPostExecute(String response) {



        }
    }


}
