package com.appzonegroup.app.fasttrack;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Bank;
import com.appzonegroup.app.fasttrack.model.TradePortServerResponse;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class TokenCashOutActivity extends BaseActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    //private SectionsPagerAdapter mSectionsPagerAdapter;

    enum InternetAction
    {
        Initialize,
        Complete
    }

    InternetAction internetAction;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager;
    //static TokenCashOutActivity thisPage;
    public String amount, phoneNumber, customerToken;//, sessionID;
    Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_cash_out);
        final EditText customerPhoneET = (EditText) findViewById(R.id.customer_phone_et);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //thisPage = this;
        // Set up the ViewPager with the sections adapter.
        //mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        customerPhoneET.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length()==11){
                    sendCustomerToken(String.valueOf(s), amount, false);
                }
            }
        });
        init();

    }

    void init()
    {
        //sessionID = UUID.randomUUID().toString().substring(0, 8);
        Spinner destinationBankSpinner = (Spinner) findViewById(R.id.spinner_destination_bank);

        ArrayList<String> bankNames = new ArrayList<>();
        bankNames.add("Select bank...");
        final ArrayList<Bank> banks = Bank.getBanks();
        for (Bank bank : banks) {
            bankNames.add(bank.getName());
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankNames);
        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        destinationBankSpinner.setAdapter(spinnerArrayAdapter);

        destinationBankSpinner.setSelection(7);
        //final View rootView = inflater.inflate(R.layout.fragment_amount_phone, container, false);

        //destinationBankSpinner.getSelectedView().setEnabled(false);
        destinationBankSpinner.setEnabled(false);

        //rootView.
            findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View view) {

            String amountString = ((EditText)findViewById(R.id.amount_et)).getText().toString().trim();
            double amount;
            try{
                amount = Double.parseDouble(amountString);
            }catch(Exception ex)
            {
                showError("Please enter a valid amount.");
                return;
            }

            if (amount <= 0)
            {
                showError("Please enter an amount greater than 0.");
                return;
            }

            String phoneNumber = ((EditText)findViewById(R.id.customer_phone_et)).getText().toString().trim();
            if (phoneNumber.length() == 0)
            {
                showError("Please enter customer's phone number.");
                return;
            }

            if (!Misc.isValidatePhoneNumber(phoneNumber))
            {
                showError("Please enter a valid phone number.");
            }

            String token = ((EditText)findViewById(R.id.customer_otp_et)).getText().toString().trim();

            if (token.length() == 0)
            {
                showError("Please enter customer's token.");
                return;
            }
            customerToken = token;

            TokenCashOutActivity.this.amount = amountString;
            TokenCashOutActivity.this.phoneNumber = phoneNumber;

            backgroundHandler = Misc.setupScheduler();
            showProgressBar("Processing...");
            internetAction = InternetAction.Initialize;
            runScheduler();
            //thisPage.mViewPager.setCurrentItem(thisPage.mViewPager.getCurrentItem() + 1, true);

            }
        });
    }

    void runScheduler() {
        myObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressBar();
                        Dialogs.getErrorDialog(TokenCashOutActivity.this, "A network-related error occurred.").show();
                    }

                    @Override
                    public void onNext(String result) {

                        hideProgressBar();

                        if (result == null)
                        {
                            showError("An error just occurred. Please ensure that you have internet and try again.");
                            return;
                        }
                        else
                        {
                            Log.e("Network call: ", result);
                        }

                        //Standard .NET additions to serialized objects
                        result = result
                                .replace("\\", "")
                                .replace("\n", "")
                                //.replace("\"", "")
                                .trim();

                        if (result.startsWith("\""))
                            result = result.substring(1, result.length() - 1);

                        Log.e("Network call: ", result);
                        TradePortServerResponse tradePortServerResponse = new Gson().fromJson(result, TradePortServerResponse.class);

                        if (tradePortServerResponse.isStatus())
                        //if (result.toLowerCase().contains("true"))
                        {
                            Dialogs.getInformationDialog(TokenCashOutActivity.this, "Transfer was successful", true).show();
                            /*dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            dialog.show();*/
                        }else
                        {
                            Dialogs.showErrorMessage(TokenCashOutActivity.this, tradePortServerResponse.getMessage());
                        }

                    }
                });
    }

    Observable<String> myObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                String result = "";
                try
                {

                    String url = "http://52.191.210.83/TradePortWebAPI/api/CreditClubCashOutService/InitiateCashOut?" +
                            "sessionId=%s" +
                            "&agentPhoneNumber=%s" +
                            "&agentInstCode=%s" +
                            "&issuingInstCode=gtb" +
                            "&tokenNumber=%s" +
                            "&amount=%s" +
                            "&customerAccount=%s";

                    String agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
                    String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

                    url = String.format(Locale.getDefault(), url, UUID.randomUUID().toString().substring(0, 8), agentPhone, institutionCode, customerToken, amount, phoneNumber);


                    //AppConstants.getBaseUrl();// + String.format(Constants.getRegisterDeviceURL(), merchantCode, terminalId);

                    /*switch (internetAction)
                    {
                        case Initialize:{
                            LocalStorage.SaveValue(AppConstants.getSessionID(), Misc.getRandomString(), getBaseContext());



                            url += "/CreditClubMiddleWareAPI/MPOSCashOut/ValidateCard?phoneNumber=%s&institutionCode=%s";
                            url = String.format(Locale.getDefault(),
                                    url,
                                    LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext()),
                                    LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext())
                            );
                            break;
                        }
                        case Complete:{
                            url += "/CreditClubMiddleWareAPI/MPOSCashOut/Transfer";
                        }
                    }*/

                    result = APICaller.postRequest(getBaseContext(), url, "{}");


                } catch (Exception e) {
                    Log.e("TokenCashOutActivity", e.getMessage());
                }

                return Observable.just(result);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_token_cash_out, menu);
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

    /*public static class AmountPhoneFragment extends Fragment
    {
        public AmountPhoneFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_amount_phone, container, false);

            rootView.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String amountString = ((EditText)rootView.findViewById(R.id.amount_et)).getText().toString().trim();
                    double amount;
                    try{
                        amount = Double.parseDouble(amountString);
                    }catch(Exception ex)
                    {
                        thisPage.showError("Please enter a valid amount.");
                        return;
                    }

                    if (amount <= 0)
                    {
                        thisPage.showError("Please enter an amount greater than 0.");
                        return;
                    }

                    String phoneNumber = ((EditText)rootView.findViewById(R.id.customer_phone_et)).getText().toString().trim();
                    if (phoneNumber.length() == 0)
                    {
                        thisPage.showError("Please enter customer's phone number.");
                        return;
                    }

                    if (!Misc.isValidatePhoneNumber(phoneNumber))
                    {
                        thisPage.showError("Please enter a valid phone number.");
                    }

                    String token = ((EditText)rootView.findViewById(R.id.customer_otp_et)).getText().toString().trim();

                    if (token.length() == 0)
                    {
                        thisPage.showError("Please enter customer's token.");
                        return;
                    }
                    thisPage.customerToken = token;



                    thisPage.amount = amountString;
                    thisPage.phoneNumber = phoneNumber;
                    thisPage.backgroundHandler = Misc.setupScheduler();
                    thisPage.showProgressBar("Processing...");
                    thisPage.internetAction = InternetAction.Initialize;
                    thisPage.runScheduler();
                    //thisPage.mViewPager.setCurrentItem(thisPage.mViewPager.getCurrentItem() + 1, true);

                }
            });



            return rootView;
        }
    }*/

    /*public static class TokenInputFragment extends Fragment
    {
        public TokenInputFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_token_input, container, false);

            rootView.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String token = ((EditText)rootView.findViewById(R.id.token_et)).getText().toString().trim();

                    if (token.length() == 0)
                    {
                        thisPage.showError("Please enter customer's token.");
                        return;
                    }
                    thisPage.customerToken = token;

                    thisPage.mViewPager.setCurrentItem(thisPage.mViewPager.getCurrentItem() + 1, true);

                }
            });
            return rootView;
        }
    }*/

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    /*public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0: default: return new AmountPhoneFragment();
                //case 1: return new TokenInputFragment();
            }
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return "Cashout Info";
        }
    }*/
}
