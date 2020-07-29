package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.Intent;

import com.appzonegroup.creditclub.pos.CardMainMenuActivity;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.adapter.CashoutMainMenuAdapter;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.MainMenuItem;
import com.appzonegroup.app.fasttrack.model.MobileAccount;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Locale;

public class CashoutMainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView lastLogin = (TextView) headerView.findViewById(R.id.last_login_tv);
        /*TextView nameTv = (TextView)headerView.findViewById(R.id.name_tv);*/

        String registrationResponse = LocalStorage.GetValueFor(AppConstants.MOBILE_ACCOUNT_JSON, getBaseContext());
        if (registrationResponse!= null)
        {
            MobileAccount mobileAccount = new Gson().fromJson(registrationResponse, MobileAccount.class);
            if (mobileAccount != null)
            {
                ((TextView)headerView.findViewById(R.id.name_tv)).setText(String.format(Locale.getDefault(),
                        "Hi, %s\n(%s)", mobileAccount.getOtherNames(), mobileAccount.getMobilePhone()));
            }
        }
        String time = LocalStorage.GetValueFor(AppConstants.LAST_LOGIN, getBaseContext());
        lastLogin.setText(time);

        //GridView gridView = findViewById(R.id.menu_grid_view);
        ListView listView = findViewById(R.id.menu_list_view);
        final ArrayList<MainMenuItem> mainMenuItems = Misc.getCashOutMainMenuItems(getBaseContext());
        CashoutMainMenuAdapter mainMenuAdapter = new CashoutMainMenuAdapter(this, mainMenuItems);
        /*gridView.setAdapter(mainMenuAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                launchMenu(mainMenuItems.get(i).getImageId());
            }
        });*/

        listView.setAdapter(mainMenuAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                launchMenu(mainMenuItems.get(i).getImageId());
            }
        });
    }

    void showNotification(String message){
        Dialogs.showErrorMessage(this, message);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView lastLogin = (TextView) headerView.findViewById(R.id.last_login_tv);
        String time = LocalStorage.GetValueFor(AppConstants.LAST_LOGIN, getBaseContext());
        lastLogin.setText(time);

    }

    @Override
    public void onBackPressed() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(CashoutMainMenuActivity.this);
        View logout_dialog = getLayoutInflater().inflate(R.layout.logout_dialog, null);
        TextView logout_header = (TextView) logout_dialog.findViewById(R.id.logout_header);
        TextView logout_message = (TextView) logout_dialog.findViewById(R.id.logout_message);
        logout_message.setText("Are you sure you want to log out?");
        Button logout_yes_button = (Button) logout_dialog.findViewById(R.id.logout_yes_btn);
        Button logout_no_button = (Button) logout_dialog.findViewById(R.id.logout_no_btn);

        builder.setView(logout_dialog);
        final AlertDialog notification = builder.create();
        notification.show();
        notification.setCanceledOnTouchOutside(false);
        notification.setCancelable(false);

        logout_no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notification.dismiss();
            }
        });

        logout_yes_button.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {

                String Time = LocalStorage.GetValueFor(AppConstants.PRESENT_LOGIN, getBaseContext());

                LocalStorage.SaveValue(AppConstants.LAST_LOGIN, Time, getBaseContext());
                notification.dismiss();
                Intent intent = new Intent(CashoutMainMenuActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });


    }

    void indicateError(String message, View view){

        showNotification(message);
        if (view != null){
            view.requestFocus();
        }
    }

    public void launchMenu(int id)//(View view)
    {
        switch (id)
        {
            case R.drawable.ic_mail:
            {
                startActivity(TokenCashOutActivity.class);
                break;
            }
            case R.drawable.ic_money:
            case R.drawable.conditional_cash_transfer: {
                startActivity(FundsTransferActivity.class);
                break;
            }
            case R.drawable.ic_credit_card:
            {
                final Dialog infoDialog = Dialogs.getDialog(R.layout.dialog_question_with_two_buttons, CashoutMainMenuActivity.this);//, "1. Insert the card into POS\n\n2. Attach the POS to your ic_phone.\n\n3. Then click OK.");
                ((TextView)infoDialog.findViewById(R.id.message_tv)).setText("1. Insert the card into POS\n\n2. Attach the POS to your ic_phone.\n\n3. Then click OK.");

                infoDialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        infoDialog.dismiss();
                    }
                });

                infoDialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        infoDialog.dismiss();
                        startActivity(CardMainMenuActivity.class);
                    }
                });
                infoDialog.show();
                break;
            }
            case R.drawable.open_account:
            {
                startActivity(OpenAccountActivity.class);
                break;
            }
            case R.drawable.loan_request:
            {
                startActivity(CreditClubLoanRequestActivity.class);
                break;
            }
            case R.drawable.bvn_update:{

                indicateError("This service is currently unavailable", findViewById(R.id.bvn_update_button));
                break;
            }

            case R.drawable.cash_deposit:{
                startActivity(DepositActivity.class);
                break;
            }
            case R.drawable.cash_withdrawal:{
                startActivity(WithdrawActivity.class);
                break;
            }

            case R.drawable.bills_payment:{
                startActivity(BillsCategoryActivity.class);
                break;
            }
            case R.drawable.interbank_withdrawal:
            {
                final Dialog dialog = Dialogs.getWithdrawalTransactionModeDialog(this);
                dialog.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = ((RadioGroup) dialog.findViewById(R.id.transaction_mode_radio_group)).getCheckedRadioButtonId();
                        if (id == -1)
                        {
                            showNotification("Please select the transaction mode");
                            return;
                        }

                        if (id == R.id.token_radio_button)
                        {
                            startActivity(TokenCashOutActivity.class);
                        }
                        else //Launch Card Reader Activity
                        {
                            final Dialog infoDialog = Dialogs.getInformationDialog(CashoutMainMenuActivity.this, "1. Insert the card into POS\n\n2. Attach the POS to your ic_phone.\n\n3. Then click OK.", false);
                            infoDialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    infoDialog.dismiss();
                                    startActivity(CardMainMenuActivity.class);
                                }
                            });
                            infoDialog.show();
                        }
                        dialog.dismiss();
                    }

                });
                dialog.show();
                break;
            }
            //This should change to the right image...... Card hotlist
            case R.drawable.ic_launcher_transparent:{
                startActivity(CardHotlistActivity.class);
                break;
            }
            //This should change to the right image...... Card linking
            case R.drawable.bg_min:{

                break;
            }
            default:
            {
                startActivity(BOILoanRequestActivity.class);
            }
        }
    }

    void startActivity(Class c)
    {
        startActivity(new Intent(this, c));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_home) {


            // Handle the reports fragment
        } else if (id == R.id.nav_reports) {
            Intent reportsIntent = new Intent(CashoutMainMenuActivity.this, ReportActivity.class);
            startActivity(reportsIntent);

            // Handles the request status fragment
        } else if (id == R.id.nav_request_status) {
            RequestStatusFragment requestStatusFragment = new RequestStatusFragment();
            FragmentManager fragmentmanager = getSupportFragmentManager();
            fragmentmanager.beginTransaction().replace(R.id.fragment_container,requestStatusFragment).commit();


            // Handle the Bills payment fragment
        } else if (id == R.id.nav_account_balance) {
            Intent billpaymentIntent = new Intent(CashoutMainMenuActivity.this, BalanceEnquiryActivity.class);
            startActivity(billpaymentIntent);

            // Handle the Bills Airtime fragment
        } else if (id == R.id.nav_airtime_top_up) {
            Intent airtimeIntent = new Intent(CashoutMainMenuActivity.this, AirtimeTopupActivity.class);
            startActivity(airtimeIntent);

        }  else*/ if (id == R.id.logout){

            String Time = LocalStorage.GetValueFor(AppConstants.PRESENT_LOGIN, getBaseContext());
            LocalStorage.SaveValue(AppConstants.LAST_LOGIN, Time, getBaseContext());
            Intent intent = new Intent(CashoutMainMenuActivity.this, LoginActivity.class);
            startActivity(intent);

            Intent logoutIntent = new Intent(CashoutMainMenuActivity.this, LoginActivity.class);
            startActivity(logoutIntent);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
