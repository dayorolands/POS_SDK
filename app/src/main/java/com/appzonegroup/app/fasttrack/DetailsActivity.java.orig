package com.appzonegroup.app.fasttrack;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.model.BVNRequest;

/**
 * Created by Oto-obong on 16/07/2017.
 */

public class DetailsActivity extends AppCompatActivity {

    String type;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();

        extras = i.getExtras();
        type = extras.getString("type");

        if(type.equalsIgnoreCase("LoanRequest")) {

            setContentView(R.layout.loanrequest_details);
            TextView AccountNumber = (TextView) findViewById(R.id.loanrequest_acctno_detail);
            TextView PhoneNumber = (TextView) findViewById(R.id.loanrequest_phoneno_detail);
            TextView Amount = (TextView) findViewById(R.id.loanrequest_amount_detail);
            TextView Status = (TextView) findViewById(R.id.loanrequest_status_detail);


            AccountNumber.setText(extras.getString("loan_accountnumber"));
            PhoneNumber.setText(extras.getString("loan_phonenumber"));
            Amount.setText(String.valueOf(extras.getString("loan_accountnumber")));
            Status.setText(extras.getString("loan_status"));

        }else if(type.equalsIgnoreCase("BVNRequest")){

            setContentView(R.layout.bvnrequest_details);
            TextView AccountNumber = (TextView) findViewById(R.id.bvnrequest_acctno_detail);
            TextView PhoneNumber = (TextView) findViewById(R.id.bvnrequest_phoneno_detail);
            TextView Status = (TextView) findViewById(R.id.bvnrequest_status_detail);

            BVNRequest bvnRequest = (BVNRequest) i.getSerializableExtra("bvnrequest");

            AccountNumber.setText(bvnRequest.getCustomerAccountNumber());
            PhoneNumber.setText(bvnRequest.getCustomerPhoneNumber());
            Status.setText(bvnRequest.getRemark());
        }
        else if(type.equalsIgnoreCase("Deposit")){

        setContentView(R.layout.deposit_details);
        TextView AccountNumber = (TextView) findViewById(R.id.deposit_acctno_detail);
        TextView Amount = (TextView) findViewById(R.id.deposit_amount_detail);
        TextView name = (TextView) findViewById(R.id.deposit_name_detail);
        TextView narration = (TextView) findViewById(R.id.deposit_narration_detail);
        TextView reference = (TextView) findViewById(R.id.deposit_reference_detail);

            AccountNumber.setText(extras.getString("deposi_Accountnumber"));
            name.setText(extras.getString("deposit_Name"));
            Amount.setText(String.valueOf(extras.getString("deposit_amount")));
            narration.setText(extras.getString("deposit_narration"));
            reference.setText(extras.getString("deposit_refference"));
    }
    }
}
