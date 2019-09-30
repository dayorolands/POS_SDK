package com.appzonegroup.app.fasttrack;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.ChangePinRequest;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.TrackGPS;
import com.appzonegroup.app.fasttrack.utility.task.PostCallTask;
import com.google.gson.Gson;




/**
 * Created by madunagu-ekene-david on 4/9/2018
 */

public class ChangePinActivity extends BaseActivity {
    EditText oldPinET, newPinET, confirmNewPinET;
    String oldPin = "";
    String newPin = "";
    String confirmNewPin = "";
    TrackGPS gps;

    String data;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        oldPinET = //(EditText)
                findViewById(R.id.old_pin);
        newPinET = //(EditText)
                findViewById(R.id.new_pin);
        confirmNewPinET = //(EditText)
                findViewById(R.id.confirm_new_pin);
        gps = new TrackGPS(ChangePinActivity.this);
        gson = new Gson();

        if(!gps.canGetLocation())
        {
            /*Double templongitude = gps.getLongitude();
            longitude = templongitude.toString();

            Double templatitude = gps.getLatitude();
            latitude = templatitude.toString();*/


            //} else {

            gps.showSettingsAlert();
        }


    }

    public void change_pin_button_click(View view) {

        String location = String.format("%s;%s", String.valueOf(gps.getLongitude()), String.valueOf(gps.getLatitude()));

        oldPin = oldPinET.getText().toString().trim();

        if (oldPin.length() == 0)
        {
            showError("Please enter the customer's old PIN");
            return;
        }

        if (oldPin.length() != 4)
        {
            showError("Please enter the complete PIN");
            return;
        }

        newPin = newPinET.getText().toString().trim();
        if (newPin.length() == 0) {
            showError("Please enter your PIN");
            return;
        }

        if (newPin.length() != 4)
        {
            showError("Please enter the complete new PIN");
            return;
        }

        confirmNewPin = confirmNewPinET.getText().toString();

        if (!confirmNewPin.equals(newPin)) {
            showError(getString(R.string.new_pin_confirmation_mismatch));
            return;
        }

        if (getPackageName().contains("creditclub"))
        {
            ChangePinRequest changePinRequest = new ChangePinRequest();
            changePinRequest.setAgentPhoneNumber(LocalStorage.getPhoneNumber(getBaseContext()));
            changePinRequest.setActivationCode(LocalStorage.GetValueFor(AppConstants.AGENT_CODE, getBaseContext()));
            changePinRequest.setInstitutionCode(LocalStorage.getInstitutionCode(getBaseContext()));
            changePinRequest.setNewPin(newPin);
            changePinRequest.setConfirmNewPin(confirmNewPin);
            changePinRequest.setOldPin(oldPin);
            changePinRequest.setGeoLocation(location);

            data = gson.toJson(changePinRequest);
            //showProgressBar(null);
            String url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/PinChange";

            new PostCallTask(getProgressDialog(), this, this).execute(url, data);
            //sendPostRequest(url,data);

        }

    }

    @Override
    public void processFinished(String result) {
        super.processFinished(result);

        if (result != null)
        {
            result = result.replace("\\", "").replace("\n", "").trim();
            com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
            if(response.isSuccessful()){
                LocalStorage.setAgentsPin(newPin, getBaseContext());
                showNotification("Your PIN was changed successfully", true);
            }
            else{
                showError(response.getReponseMessage());
            }
        }
        else
        {
            showError("A network-related error just occurred. Please try again later");
        }

    }

    /*public void sendPostRequest(String url, String data){
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject convertedObject = null;
        try {
            convertedObject = new JSONObject(data);
        }
        catch (Exception e){
            Log.e("creditclub","failed json parsing");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,convertedObject, new com.android.volley.Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject object) {
                String result = object.toString();
                result = result.replace("\\", "").replace("\n", "").trim();
                com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
                if(response.isSuccessful()){
                    LocalStorage.setAgentsPin(newPin, getBaseContext());
                    showNotification("Your PIN was changed successfully", true);
                }
                else{
                    showError(response.getReponseMessage());
                }
            }


        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showError("A network-related error just occurred. Please try again later");
            }

        });
        queue.add(request);
    }*/

}


