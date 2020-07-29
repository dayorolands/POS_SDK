package com.appzonegroup.app.fasttrack;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Response;
import com.appzonegroup.app.fasttrack.model.TokenRequest;
import com.appzonegroup.app.fasttrack.model.WithdrawalRequest;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.gson.Gson;

import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class WithdrawalByAPITokenActivity extends BaseActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    static EditText agentPINEt, customerAccountNoEt,
            amountEt, customerTokenEt, customerPINEt;


    enum InternetAction
    {
        SendToken,
        Withdraw
    }

    Handler handler;
    Gson gson;

    InternetAction internetAction;

    WithdrawalRequest withdrawalRequest;
    TokenRequest tokenRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal_by_apitoken);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        handler = Misc.setupScheduler();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        gson = new Gson();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_withdrawal_by_apitoken, menu);
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

    public void send_token_clicked(View view) {

        if (customerAccountNoEt.getText().toString().trim().length() == 0)
        {
            showError("Please enter the customer's account number");
            return;
        }

        if (agentPINEt.getText().toString().length() == 0)
        {
            showError("Please enter your PIN");
            return;
        }

        tokenRequest = new TokenRequest();
        tokenRequest.setAgentPhoneNumber(LocalStorage.getPhoneNumber(getBaseContext()));
        tokenRequest.setInstitutionCode(LocalStorage.getInstitutionCode(getBaseContext()));
        tokenRequest.setAgentPin(agentPINEt.getText().toString());
        tokenRequest.setCustomerAccountNumber(customerAccountNoEt.getText().toString().trim());

        internetAction = InternetAction.SendToken;
        showProgressBar("Sending token to customer");

    }

    public void withdraw_clicked(View view) {

        String amount = amountEt.getText().toString().trim();
        if (amount.length() == 0)
        {
            showError("Please enter the amount to withdraw");
            return;
        }

        try
        {
            Double.parseDouble(amount);
        }catch (Exception ex)
        {
            showError("Please enter a valid amount");
            return;
        }

        if (customerTokenEt.getText().toString().length() == 0)
        {
            showError("Please enter the token sent to the customer");
            return;
        }

//        if (customerPINEt.getText().toString().length() == 0)
//        {
//            showError("Enter the customer PIN");
//            return;
//        }

        withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setAgentPhoneNumber(tokenRequest.getAgentPhoneNumber());
        withdrawalRequest.setInstitutionCode(tokenRequest.getInstitutionCode());
        withdrawalRequest.setCustomerAccountNumber(tokenRequest.getCustomerAccountNumber());
        withdrawalRequest.setAgentPin(tokenRequest.getAgentPin());
        withdrawalRequest.setToken(customerTokenEt.getText().toString());
        withdrawalRequest.setCustomerPin(customerPINEt.getText().toString());
        withdrawalRequest.setAmount(amount);
        withdrawalRequest.setGeoLocation(getGps().getGeolocationString());

        showProgressBar("Sending withdrawal request...");
        internetAction = InternetAction.Withdraw;
        runScheduler();
    }

    void runScheduler() {
        myObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(handler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressBar();
                        Dialogs.getErrorDialog(WithdrawalByAPITokenActivity.this, "An network-related error occurred.").show();
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

                        Log.e("Network call: ", result);
                        Response response = gson.fromJson(result, Response.class);

                        if (!response.isSuccessful())
                        {
                            showError(response.getReponseMessage());
                            return;
                        }

                        switch (internetAction)
                        {
                            case SendToken:
                            {
                                showNotification("Token was sent to the customer successfully");
                                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                                break;
                            }
                            case Withdraw:
                            {
                                Dialogs.getInformationDialog(WithdrawalByAPITokenActivity.this, "Deposit was successful", true).show();
                                /*dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                                dialog.show();*/
                                break;
                            }
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
                    String url = null;
                    String data = "{}";
                    switch (internetAction)
                    {
                        case SendToken:
                        {
                            url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/api/SendToken";
                            data = gson.toJson(tokenRequest);
                            break;
                        }
                        case Withdraw:
                        {
                            url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/api/WithDrawal";
                            data = gson.toJson(withdrawalRequest);
                        }
                    }



                    result = APICaller.postRequest(getBaseContext(), url, gson.toJson(data));


                } catch (Exception e) {
                    Log.e("CC:Deposit", e.getMessage());
                }

                return Observable.just(result);
            }
        });
    }

    public static class CustomerInfoFragment extends Fragment {

        public CustomerInfoFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_customer_info, container, false);

            agentPINEt = rootView.findViewById(R.id.agent_pin_et);
            customerAccountNoEt = rootView.findViewById(R.id.customer_account_number_et);

            return rootView;
        }
    }

    public static class OtherInfoFragment extends Fragment {

        public OtherInfoFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_customer_info, container, false);

            amountEt = rootView.findViewById(R.id.amount_et);
            customerTokenEt = rootView.findViewById(R.id.customer_token_et);
            customerPINEt = rootView.findViewById(R.id.customer_pin_et);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position)
            {
                case 0:default: return new CustomerInfoFragment();
                case 1: return new OtherInfoFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
