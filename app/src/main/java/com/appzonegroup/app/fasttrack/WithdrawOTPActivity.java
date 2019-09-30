package com.appzonegroup.app.fasttrack;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.DepositWithdrawalResponse;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

/**
 * Created by Eshiett on 3/5/2017.
 */



public class WithdrawOTPActivity extends AppCompatActivity {

    Bundle extras;
    String temptoken, token,accountNo,amount,narration,reference;
    EditText tokenET;
    static Button deposit_dialog_button;
    static TextView deposit_header, deposit_message;
    static ProgressBar deposit_progressbar;
    static ImageView deposit_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        extras = i.getExtras();

        accountNo = extras.getString("accountNo");
        amount = extras.getString("amount");
        narration = extras.getString("narration");
        reference = extras.getString("transactionReference");

        setContentView(R.layout.activity_withdraw_otp);
        tokenET = (EditText) findViewById(R.id.withdraw_token_et);

    }


    void showNotification(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }


    void indicateError(String message, View view) {

        showNotification(message);
        if (view != null) {
            view.requestFocus();
        }
    }


    public void otpAuntentication(View view){

        token = tokenET.getText().toString();

        if(token.length() == 0){

            indicateError("Please enter a token", tokenET);
            return;
        }



            final AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawOTPActivity.this);
            View deposit_view = getLayoutInflater().inflate(R.layout.deposit_dialog, null);
            deposit_header = (TextView) deposit_view.findViewById(R.id.deposit_header);
            deposit_message = (TextView) deposit_view.findViewById(R.id.deposit_message);
            deposit_image = (ImageView) deposit_view.findViewById(R.id.deposit_dialog_btn_imageview);
            deposit_progressbar = (ProgressBar)deposit_view.findViewById(R.id.deposit_progressbar);
            deposit_dialog_button = (Button) deposit_view.findViewById(R.id.deposit_dialog_btn);
            deposit_header.setText("WITHDRAWAL IN PROGRESS");
            deposit_message.setText("please wait\n withdrawal in progress");




        
            builder.setView(deposit_view);
            final AlertDialog deposit_dialog = builder.create();
            deposit_dialog.show();
            deposit_dialog.setCanceledOnTouchOutside(false);

            deposit_dialog_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deposit_dialog.dismiss();
                    Intent i = new Intent(WithdrawOTPActivity.this, Menu3Activity.class);
                    startActivity(i);
                }
            });

            SyncWithdrawal syncWithdrawal = new SyncWithdrawal(WithdrawOTPActivity.this, accountNo, amount,narration,reference,token, deposit_dialog);
            syncWithdrawal.execute();


    }

    private class SyncWithdrawal extends AsyncTask<String, String, String> {

        Context context;
        String accountNo;
        String amount;
        String narration;
        String reference;
        String pin;
        String token;
        AlertDialog deposit_dialog;
        Gson gson = new Gson();


        SyncWithdrawal(Context context, String accountNo, String amount,String narration,String reference,String token,AlertDialog deposit_dialog){
            this.context = context;
            this.accountNo = accountNo;
            this.amount = amount;
            this.deposit_dialog = deposit_dialog;
            this.narration = narration;
            this.reference = reference;
            this.token = token;

        }

        @Override
        protected void onPreExecute() {

            Toast.makeText(getBaseContext(), "Withdrawal Processing, Please wait ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {

            String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
            String agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
            String agentPIN = LocalStorage.GetValueFor(AppConstants.AGENT_PIN, getBaseContext());
            String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

            String url = AppConstants.getAccessUrl() + "CompleteWithdrawal?token="+token+"&institutionCode="+institutionCode+"&customerAccountNumber="+accountNo+"&agentPhoneNumber="+agentPhone+"&amount="+amount+"&narration="+narration+"&transactionReference="+reference;

            //String requestJson =  "{ \"token\" : " +  "\""+token+"\"" +", \"institutionCode\"  : " + "\""+ institutionCode +"\"" + ",\"debitphoneNumber\":" + "\"" + agentPhone + "\"" + ",\"accountNoFrom\":" + "\"" + accountNo + "\"" + ",\"accountNoTo\":" + "\""+accountNo+"\"" +  ",\"amount\":" + "\""+amount+"\"" + ",\"narration\":" + "\""+narration+"\"" + ",\"transactionReference\":" + "\""+reference+"\"" +  "}";


            String response = APICaller.makePostRequestNoJson(url,Token);



            DepositWithdrawalResponse serverResponse;
            try {

                serverResponse = gson.fromJson(response, DepositWithdrawalResponse.class);

                if (serverResponse.getStatus().equalsIgnoreCase("00")) {

                    return "Successful";

                }else {

                    Crashlytics.logException(new Exception(serverResponse.getModelResponse().getResponseMessage()));

                    return serverResponse.getModelResponse().getResponseMessage();

                }




            }catch (Exception ex){

                Crashlytics.logException(new Exception(ex.getMessage()));

                return "An error has occurred";

            }

        }

        @Override
        protected void onPostExecute(String response) {


            if(response.equalsIgnoreCase("An error has occurred")) {

                deposit_header.setText("Withdrawal Failed");
                deposit_message.setText("An error in connection has occured");
                deposit_progressbar.setVisibility(View.GONE);
                deposit_image.setVisibility(View.VISIBLE);
                deposit_image.setImageDrawable(context.getResources().getDrawable(R.drawable.close_button));
                deposit_dialog_button.setVisibility(View.VISIBLE);

                Toast.makeText(getBaseContext(), "An error has occurred", Toast.LENGTH_LONG).show();


            }else if(response.equalsIgnoreCase("Successful")) {

                deposit_header.setText("Withdrawal Successful");
                deposit_message.setText(response);
                deposit_progressbar.setVisibility(View.GONE);
                deposit_image.setVisibility(View.VISIBLE);
                deposit_image.setImageDrawable(context.getResources().getDrawable(R.drawable.check_button));
                deposit_dialog_button.setVisibility(View.VISIBLE);

            }else{

                deposit_header.setText("Withdrawal Failed");
                deposit_message.setText(response);
                deposit_progressbar.setVisibility(View.GONE);
                deposit_image.setVisibility(View.VISIBLE);
                deposit_image.setImageDrawable(context.getResources().getDrawable(R.drawable.close_button));
                deposit_dialog_button.setVisibility(View.VISIBLE);



            }
        }
    }

}
