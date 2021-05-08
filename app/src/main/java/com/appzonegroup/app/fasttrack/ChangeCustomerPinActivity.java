package com.appzonegroup.app.fasttrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.ChangePinRequest;
import com.appzonegroup.app.fasttrack.model.TokenRequest;
import com.appzonegroup.app.fasttrack.ui.NonSwipeableViewPager;
import com.appzonegroup.app.fasttrack.utility.FunctionIds;
import com.appzonegroup.app.fasttrack.utility.task.PostCallTask;
import com.creditclub.core.ui.widget.DialogListener;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * Created by madunagu-ekene-david on 4/19/2018.
 */

public class ChangeCustomerPinActivity extends BaseActivity {
    static EditText oldPinET, newPinET, confirmNewPinET, customerTokenET, customerAccountNoEt, customerPhoneEt;
    String oldPin = "";
    String newPin = "";
    String confirmNewPin = "";
    String customerToken = "";
    String customerAccount = "";

    @Nullable
    @Override
    public Integer getFunctionId() {
        return FunctionIds.CUSTOMER_CHANGE_PIN;
    }

    String data;
    Gson gson;

    private ChangeCustomerPinActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private NonSwipeableViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_customer_pin);

        gson = new Gson();

        mSectionsPagerAdapter = new ChangeCustomerPinActivity.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.setClickable(false);

        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());


    }

    public void send_customer_token_click(View view) {

        customerAccount = customerAccountNoEt.getText().toString().trim();

        if (customerAccount.length() == 0) {
            showError(getString(R.string.please_enter_the_account_number));
            return;
        }

        if (customerAccount.length() != 10) {
            showError(getString(R.string.please_enter_the_complete_account_number));
            return;
        }

        sendCustomerToken(customerAccount, customerAccountNoEt);
    }

    public void sendCustomerToken(String customerPhoneNumber, final EditText phoneNumberET) {
        //make the phone number EditText uneditable while sending the token
        phoneNumberET.setEnabled(false);
        TokenRequest tkRequest = new TokenRequest();
        tkRequest.setCustomerAccountNumber(customerPhoneNumber);
        tkRequest.setAgentPhoneNumber(getLocalStorage().getAgentPhone());
        tkRequest.setAgentPin("0000");
        tkRequest.setInstitutionCode(getLocalStorage().getInstitutionCode());
        tkRequest.setPinChange(true);
        String data = new Gson().toJson(tkRequest);
        Response.Listener<JSONObject> doOnsuccess = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject object) {
                String result = object.toString();
                com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
                if (response.isSuccessful()) {
                    showSuccess(response.getReponseMessage());
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                } else {
                    indicateError(response.getReponseMessage(), phoneNumberET);
                }
            }

        };
        Response.ErrorListener doOnError = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                indicateError("A network-related error occurred while sending token", phoneNumberET);
            }

        };
        sendJSONPostRequestWithCallback(AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/SendToken", data, doOnsuccess, doOnError);

    }

    public void change_pin_button_click(View view) {

        String location = getGps().getGeolocationString();

        oldPin = oldPinET.getText().toString();

        if (oldPin.length() != 4) {
            indicateError("Please enter the old PIN", oldPinET);
            return;
        }

        newPin = newPinET.getText().toString().trim();
        if (newPin.length() == 0) {
            indicateError("Please enter your PIN", newPinET);
            return;
        }

        confirmNewPin = confirmNewPinET.getText().toString();

        if (!confirmNewPin.equals(newPin)) {
            indicateError(getString(R.string.new_pin_confirmation_mismatch), confirmNewPinET);
            return;
        }

        if (customerPhoneEt.getText().toString().trim().length() != 11) {
            indicateError(getString(R.string.please_enter_customers_phone_number), customerPhoneEt);
            return;
        }


        customerToken = customerTokenET.getText().toString();

        if (customerToken.length() == 0) {
            indicateError("Token cannot be empty", customerTokenET);
            return;
        }

        //if (getPackageName().endsWith("revamped"))
        {
            ChangePinRequest changePinRequest = new ChangePinRequest();
            changePinRequest.setAgentPhoneNumber(getLocalStorage().getAgentPhone());
            changePinRequest.setInstitutionCode(getLocalStorage().getInstitutionCode());
            changePinRequest.setNewPin(newPin);
            changePinRequest.setConfirmNewPin(confirmNewPin);
            changePinRequest.setOldPin(oldPin);
            changePinRequest.setGeoLocation(location);
            changePinRequest.setCustomerPhoneNumber(customerPhoneEt.getText().toString().trim());
            changePinRequest.setAgentPin("0000");
            changePinRequest.setCustomerToken(customerToken);

            data = gson.toJson(changePinRequest);
            showProgressBar("Processing");
            String url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/PinChange";
            //sendPostRequest(url,data);
            new PostCallTask(getDialogProvider(), this, this).execute(url, data);

        }

    }

    @Override
    public void processFinished(String output) {
        super.processFinished(output);

        if (output == null) {
            showError(getString(R.string.network_error_message));
            return;
        }

        com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(output, com.appzonegroup.app.fasttrack.model.Response.class);

        if (response.isSuccessful()) {
            showSuccess("Pin Changed Successfully", new Function1<DialogListener<? extends Object>, Unit>() {
                @Override
                public Unit invoke(DialogListener<?> dialogListener) {
                    dialogListener.onClose(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            finish();
                            return null;
                        }
                    });
                    return null;
                }
            });
        } else {
            showError(response.getReponseMessage());
        }

    }

    public static class CustomerAccountNumberFragment extends Fragment {

        public CustomerAccountNumberFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_account_number, container, false);

            customerAccountNoEt = (//(EditText)
                    rootView.findViewById(R.id.customer_account_number_et));

            return rootView;
        }
    }

    public static class ChangePinFragment extends Fragment {

        public ChangePinFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_pin_details, container, false);

            oldPinET = (//(EditText)
                    rootView.findViewById(R.id.old_pin_et));
            newPinET = (//(EditText)
                    rootView.findViewById(R.id.new_pin_et));
            confirmNewPinET = (//(EditText)
                    rootView.findViewById(R.id.confirm_new_pin_et));
            customerTokenET = (//(EditText)
                    rootView.findViewById(R.id.customer_token_et));
            customerPhoneEt = rootView.findViewById(R.id.customer_phone_et);

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
            if (position == 1) {
                return new ChangePinFragment();
            }
            return new CustomerAccountNumberFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Account Number";
                case 1:
                    return "Pin Details";
            }
            return null;
        }
    }


}


