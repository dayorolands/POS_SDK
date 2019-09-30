package com.appzonegroup.app.fasttrack;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.dataaccess.AssociationDAO;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Association;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.AppStatus;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class Menu2Activity extends AppCompatActivity {

    //String customerResult = "";
    //ArrayList<Customer> allCustomers;
    AppStatus appStatus = new AppStatus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);

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


    @Override
    public void onBackPressed() {

    }

    public void take_action(View view)
    {
        Intent intent;
        switch (view.getId())
        {
            case R.id.open_account_button:
            {
                intent = new Intent(getBaseContext(), OpenAccountActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.loan_request_button:
            {
                intent = new Intent(getBaseContext(), CreditClubLoanRequestActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.bvn_update_button:{
                intent = new Intent(getBaseContext(), BVNUpdateActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.bvn_deposit_button:{
                if (appStatus.getInstance(this).isOnline()) {


                    intent = new Intent(getBaseContext(), DepositActivity.class);
                    startActivity(intent);
                    break;

                } else {

                    indicateError("Please connect to the internet to use this option ",findViewById(R.id.bvn_deposit_button));
                    break;
                }

            }

            case R.id.bvn_withdraw_button:{
                if (appStatus.getInstance(this).isOnline()) {
                intent = new Intent(getBaseContext(), WithdrawActivity.class);
                startActivity(intent);
                break;
                } else {

                    indicateError("Please connect to the internet to use this option ",findViewById(R.id.bvn_withdraw_button));
                    break;
                }
            }


            case R.id.successfully_sync_account_report_button:
            {

                break;
            }

            case R.id.loan_requests_report_button:{
                break;
            }
            case R.id.failed_loan_requests_report_button:{

                break;
            }

            case R.id.reload_customers_button:{
                //Toast.makeText(getBaseContext(), "Fetching customers", Toast.LENGTH_LONG).show();
                //fetchCustomers();
                break;
            }

            case R.id.logout_button:{

                break;
            }
            case R.id.reload_association_button:{
                Toast.makeText(getBaseContext(), "Fetching your associations in background", Toast.LENGTH_LONG).show();
                getAssociation();
                break;
            }
            default:
            {
                intent = new Intent(getBaseContext(), BOILoanRequestActivity.class);
                startActivity(intent);
            }
        }
    }

    private void getAssociation(){
        new Thread(){
            public void run(){
                String url = AppConstants.getBaseUrl() + "Get/GetAssociations";
                Observable<String> observable = Observable.from(new String[]{APICaller.makeGetRequest2(url)});

                observable.subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e("Associations call", s + "");
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<Association>>() {}.getType();
                        ArrayList<Association> associations = gson.fromJson(s, type);
                        if (associations == null){
                            Toast.makeText(getBaseContext(), "No associations downloaded", Toast.LENGTH_LONG).show();
                            return;
                        }

                        /*if (associations.size() == 0){
                            showNotification("No associations downloaded");
                            return;
                        }*/

                        AssociationDAO associationDAO = new AssociationDAO(getBaseContext());

                        if (associations.size() > 0){
                            associationDAO.Insert(associations);
                            LocalStorage.SaveValue(AppConstants.ASSOCIATIONS, AppConstants.ASSOCIATIONS, getBaseContext());
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (LocalStorage.GetValueFor(AppConstants.LOAN_PRODUCTS, getBaseContext()) != null &&
                                                LocalStorage.GetValueFor(AppConstants.PRODUCTS, getBaseContext()) != null){

                                }
                            }
                        });

                    }
                });
            }
        }.start();
    }

    /*private void fetchCustomers(){
        new Thread(){
            public void run(){
                int start = 0, limit = 5;
                String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());
                final CustomerDAO customerDAO = new CustomerDAO(getBaseContext());
                allCustomers = new ArrayList<>();

                for(;;) {

                    String url = AppConstants.getBaseUrl() + "Get/GetAllCustomers?institutionCode="+ institutionCode
                            + "&start=" + start + "&limit=" + limit;

                    Observable<String> observable = Observable.from(new String[]{APICaller.makeGetRequest(url)});

                    observable.subscribe(new Action1<String>() {
                        @Override
                        public void call(String s)
                        {
                            customerResult = s;
                            Log.e("Customers call", s + "");
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Customer>>() {}.getType();
                            ArrayList<Customer> customers = gson.fromJson(s, type);

                            if (customers.size() > 0) {

                                allCustomers.addAll(customers);

                                //customerDAO.Insert(customers);
                                //LocalStorage.SaveValue(AppConstants.CUSTOMERS, AppConstants.CUSTOMERS, getBaseContext());
                            }

                        }
                    });

                    start += limit;

                    if (customerResult.trim().equals("[]"))
                    {
                        if (allCustomers.size() > 0){
                            customerDAO.Insert(allCustomers);
                        }
                        break;
                    }



                }
            }
        }.start();
    }*/

}
