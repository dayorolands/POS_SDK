package com.appzonegroup.app.fasttrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.creditclub.core.data.response.GenericResponse;
import com.appzonegroup.app.fasttrack.model.jsonbody.PayBillItemModel;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.gson.Gson;

/**
 * Created by Oto-obong on 25/09/2017.
 */

public class PayBillOTPActivity extends AppCompatActivity {

    EditText billPaymentOTP, billPayment_CustomerNo, billPayment_CustomerEmail;
    PayBillItemModel payBillItemModel;
    Gson gson;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paybill_otp);

        gson = new Gson();

        Intent i = getIntent();

        payBillItemModel = (PayBillItemModel) i.getSerializableExtra("payBillItemModel");

        billPaymentOTP = (EditText) findViewById(R.id.payBill_token_et);

        billPayment_CustomerNo = (EditText) findViewById(R.id.paybill_customerno_et);

        billPayment_CustomerEmail = (EditText) findViewById(R.id.paybill_customeremail_et);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    public void confirmOTP(View view){

        if(billPaymentOTP.getText().toString().length() == 0){

            Toast.makeText(this, "Please enter your OTP", Toast.LENGTH_LONG).show();

            return;
        }

        if(billPayment_CustomerNo.getText().toString().length() == 0){

            Toast.makeText(this, "Please enter the Customer's phone number", Toast.LENGTH_LONG).show();

            return;
        }

        if(billPayment_CustomerEmail.getText().toString().length() == 0){

            Toast.makeText(this, "Please enter the Customer's email", Toast.LENGTH_LONG).show();

            return;
        }

        payBillItemModel.setOtp(billPaymentOTP.getText().toString());
        payBillItemModel.setCustomerPhoneNumber(billPayment_CustomerNo.getText().toString());
        payBillItemModel.setEmail(billPayment_CustomerEmail.getText().toString());

        progressDialog = new ProgressDialog(PayBillOTPActivity.this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...processing payment");
        progressDialog.show();


        ConfirmOTPSync  confirmOTPSync = new ConfirmOTPSync(PayBillOTPActivity.this, gson, payBillItemModel);
        confirmOTPSync.execute();


    }


    private class ConfirmOTPSync extends AsyncTask<String,String, String> {

        Context context;
        Gson gson;
        PayBillItemModel payBillItemModel;
        String successMessage;
        String Message;

        public ConfirmOTPSync(Context context,Gson gson, PayBillItemModel payBillItemModel){

            this.context = context;
            this.gson = gson;
            this.payBillItemModel = payBillItemModel;


        }

        @Override
        protected void onPreExecute() {

            gson = new Gson();

        }



        @Override
        protected String doInBackground(String... params) {




            String url = Misc.payBillItemUrl(payBillItemModel);


            String response = APICaller.makePostRequestOneParam(url);
            //Log.e("Sync Ac", response);


            try {

                GenericResponse serverResponse = gson.fromJson(response, GenericResponse.class);

                if (serverResponse.getResponseCode().equals("00")) {

                    successMessage = serverResponse.getResponseMessage();

                    return serverResponse.getResponseCode();

                } else {

                    Message = serverResponse.getResponseMessage();

                    return serverResponse.getResponseCode();

                }


            }catch (Exception ex){
                return null;

            }

        }

        @Override
        protected void onPostExecute(String response) {

            if(response == null){

                progressDialog.dismiss();

                Dialogs.getAlertDialog(PayBillOTPActivity.this, "Connection lost, Please try again!").show();


            }else if(response.equals("00")){

                AlertDialog.Builder builder1 = new AlertDialog.Builder(PayBillOTPActivity.this);
                builder1.setMessage(successMessage);
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                Intent intent = new Intent(PayBillOTPActivity.this,Menu3Activity.class);
                                startActivity(intent);

                            }
                        });

                AlertDialog alert11 = builder1.create();



            }else{

                progressDialog.dismiss();

                Dialogs.getAlertDialog(PayBillOTPActivity.this, Message).show();


            }
        }
    }
}
