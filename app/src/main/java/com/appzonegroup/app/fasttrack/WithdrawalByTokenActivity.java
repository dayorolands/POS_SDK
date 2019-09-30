package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.utility.Dialogs;

public class WithdrawalByTokenActivity extends AppCompatActivity implements View.OnClickListener{

    //ArrayList<Institution> institutions;
    Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal_by_token);

        loadingDialog = Dialogs.getProgress(this, "Getting all institutions...");
        loadingDialog.show();

        /*new Thread(new Runnable() {
            @Override
            public void run() {

                String institutionData = APICaller.getRequest(getBaseContext(), AppConstants.getBaseUrl() + "/CreditClubOffline/api/Get/GetInstitutions");

                if (institutionData == null)
                {
                    showMessage("We could not get the institutions");
                    return;
                }

                TypeToken<ArrayList<Institution>> institutionType = new TypeToken<ArrayList<Institution>>(){};
                institutions = new Gson().fromJson(institutionData, institutionType.getType());

                final ArrayList<String> institutionNames = new ArrayList<>();
                institutionNames.add("Please select an institution...");

                for (Institution institution : institutions)
                    institutionNames.add(institution.getName());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> institutionsArray = new ArrayAdapter<>(WithdrawalByTokenActivity.this,android.R.layout.simple_spinner_item, institutionNames);
                        institutionsArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ((Spinner)findViewById(R.id.institutions_spinner)).setAdapter(institutionsArray);
                        loadingDialog.dismiss();
                    }
                });




            }
        }).start();*/

        findViewById(R.id.submit_btn).setOnClickListener(this);
    }

    void showMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog.isShowing())
                    loadingDialog.dismiss();

                Dialogs.showErrorMessage(WithdrawalByTokenActivity.this, message);
            }
        });
    }

    @Override
    public void onClick(View view) {

        /*int index = ((Spinner)findViewById(R.id.institutions_spinner)).getSelectedItemPosition();
        if (index == 0)
        {
            Toast.makeText(getBaseContext(), "Please select an institution", Toast.LENGTH_LONG).show();
            return;
        }

        String institutionCode = institutions.get(index - 1).getInstitutionCode();*/


        String phoneNo = ((EditText)findViewById(R.id.phone_no_et)).getText().toString().trim();

        if (phoneNo.length() != 11)
        {
            Toast.makeText(getBaseContext(), "Please enter correct phone number", Toast.LENGTH_LONG).show();
            return;
        }

        String amountString = ((EditText)findViewById(R.id.amount_et)).getText().toString().trim();
        double amount;
        try
        {
            amount = Double.parseDouble(amountString);
        }catch (Exception ex)
        {
            Toast.makeText(getBaseContext(), "Please enter a valid amount", Toast.LENGTH_LONG).show();
            return;
        }

        if (amount < 0)
        {
            Toast.makeText(getBaseContext(), "You cannot cash out a negative amount. Please enter a valid amount", Toast.LENGTH_LONG).show();
            return;
        }

        String token = ((EditText)findViewById(R.id.token_et)).getText().toString().trim();

        if (token.length() == 0)
        {
            Toast.makeText(getBaseContext(), "The token is invalid.", Toast.LENGTH_LONG).show();
            return;
        }


        Toast.makeText(getBaseContext(), "Nothing to process for now.", Toast.LENGTH_LONG).show();
    }
}
