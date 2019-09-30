package com.appzonegroup.app.fasttrack;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Balance;
import com.appzonegroup.app.fasttrack.model.BalanceEnquiry;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by Oto-obong on 12/10/2017.
 */

public class AccountDetails extends BaseActivity {

    EditText customers_accountno_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_details);

        customers_accountno_et = //(EditText)
                findViewById(R.id.bal_customer_account_number_et);

    }

    public void getCustomersBal(View v){
        showProgressBar("Processing");
        if(customers_accountno_et.getText().toString().length() == 0){

            indicateError("Please enter an account number",customers_accountno_et);
        }
        String customerAccountNumber = customers_accountno_et.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject convertedObject = null;

        String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
        String phoneNumber = LocalStorage.getPhoneNumber(getBaseContext());
        String urlString = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/BalanceEnquiry";
        BalanceEnquiry balanceEnquiry = new BalanceEnquiry();
        balanceEnquiry.setAgentPin(LocalStorage.getAgentsPin(getBaseContext()));
        balanceEnquiry.setCustomerAccountNumber(customerAccountNumber);
        balanceEnquiry.setAgentPhoneNumber(LocalStorage.getPhoneNumber(getBaseContext()));
        balanceEnquiry.setInstitutionCode(LocalStorage.getInstitutionCode(getBaseContext()));
        String data = new Gson().toJson(balanceEnquiry);
        try {
            convertedObject = new JSONObject(data);
        }
        catch (Exception e){
            Log.e("creditclub","failed json parsing");
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, urlString,convertedObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject object) {
                String result = object.toString();
                if(result.contains("Balance")){
                    Balance response = new Gson().fromJson(result, Balance.class);
                    if(response.isSussessful()){
                        showSuccess(response.getResponseMessage());
                    }
                }
                else {
                    com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
                    showNotification(response.getReponseMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showError("Network Error Occured");
            }
        });
        queue.add(request);
    }





}
