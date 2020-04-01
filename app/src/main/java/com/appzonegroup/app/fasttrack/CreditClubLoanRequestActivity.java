package com.appzonegroup.app.fasttrack;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appzonegroup.app.fasttrack.dataaccess.AssociationDAO;
import com.appzonegroup.app.fasttrack.model.*;
import com.appzonegroup.app.fasttrack.utility.CustomAutoCompleteAdapter;
import com.appzonegroup.app.fasttrack.utility.FunctionIds;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class CreditClubLoanRequestActivity extends CustomerBaseActivity {

    Handler backgroundHandler;
    //private String accountNumber, amount, phoneNumber, associationID, ;

    ArrayList<Association> associations;
    ArrayList<LoanProduct> eligibleLoanProducts;

    @Nullable
    @Override
    public Integer getFunctionId() {
        return FunctionIds.LOAN_REQUEST;
    }

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    static EditText loanRequest_customerPhone_et,
    //loanRequest2_customerName_et,
    loanRequest_marketAssociations_et,
            loanRequest_memberID_et,
            loanRequest_customerAccount_et,
            loanRequest_loanAmount_et,
    //loanRequest2_bvn_et,
    //loanRequest2_productid_et,
    //loanRequest2_customerid_et,
    agentPIN_et;

    static Spinner loanProductsSpinner;
    //static AutoCompleteTextView associationAutoCompletTV;
    /*AutoCompleteTextView loanRequest_institutions_actv;
    Spinner
            loanRequest2_loanProducts_spinner;*/

    TextView lr_header, lr_message;
    ProgressBar lr_progressbar;

    static CustomAutoCompleteAdapter adapter;

    LinearLayout wrapper;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private int associationCount = 0;
    private long localAssociationsCount = 0;
    public int startIndex = 0;
    public int increment = 1000;
    //public AssociationList associationList;
    private AssociationDAO associationDAO;
    //private static String selectedAssociation;
    private static String selectedAssociationID = "";

    @Override
    public void onCustomerReady(Bundle savedInstanceState) {
        setContentView(R.layout.activity_loan_request);

        backgroundHandler = Misc.setupScheduler();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.clearOnTabSelectedListeners();


        associationDAO = new AssociationDAO(getBaseContext());
        localAssociationsCount = associationDAO.count();

        //getAssociationList();
    }

    public String getNextUrl() {
        String url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/GetAssociations?insti=" + LocalStorage.getInstitutionCode(getBaseContext()) + "&startIndex=" + startIndex + "&limit=" + increment;
        return url;
    }


    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        } else {
            super.onBackPressed();
        }
    }

    private void incrementCounters() {
        startIndex = startIndex + increment;

        //for now
        localAssociationsCount = startIndex;
        String loaderText = "Getting associations " + localAssociationsCount + " of " + associationCount;
        showProgressBar(loaderText);
    }


    /*public void getAssociationList(){
        showProgressBar("Loading...");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url =  "https://api.mybankone.com/CreditClubMiddleWareAPI/CreditClubStatic/GetAssociations?insti=000000&startIndex=0&limit=0";
        StringRequest getAssociationCount = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if (response != null) {
                            try
                            {
                                //Standard .NET additions to serialized objects
                                response = response.replace("\\", "").replace("\n", "").trim();
                                associationList = new Gson().fromJson(response, AssociationList.class);
                                associationCount = associationList.getTotalCount();
                                if(associationCount!=localAssociationsCount){
                                    associationDAO.RecreateTable();
                                    getAssociationsIteratively();
                                }
                                else{
                                    updateAssociationsAutoComplete(associationDAO.getList());
                                    showSuccess(" Association List is Updated");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                                showError(e.getMessage());
                            }
                        } else {
                            showError("Api call failed");
                        };
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                updateAssociationsAutoComplete(associationDAO.getList());
                showError("A network-related error occurred.");
            }
        });
        queue.add(getAssociationCount);
    }

    private com.android.volley.Response.Listener<String> getnextCallback(){
        return new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                if (response != null) {
                    try
                    {
                        //Standard .NET additions to serialized objects
                        response = response.replace("\\", "").replace("\n", "").trim();
                        associationList = new Gson().fromJson(response, AssociationList.class);
                        associationDAO.Insert(associationList.getAssociations());

                        //increment url counters here
                        incrementCounters();

                        if(associationCount!=0 && localAssociationsCount < associationCount){
                            getAssociationsIteratively();
                        }
                        else {
                            //here finish the loader
                            updateAssociationsAutoComplete(associationDAO.getList());
                            showSuccess("Associations Gotten successfully");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                        showError(e.getMessage());
                    }
                } else {
                    Log.e("ResponseFailed","Api call failed");
                    showError("Api call failed");
                };
            }
        };
    }

    public void getAssociationsIteratively(){
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getNextUrl(),getnextCallback(), new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getAssociationsIteratively();
            }
        });

        queue.add(stringRequest);
    }
    */

    public void getLoanProducts() {
        String url = String.format(Locale.getDefault(),
                "%s/CreditClubMiddleWareAPI/CreditClubStatic/GetEligibleLoanProducts?institutionCode=%s&associationID=%s&memberID=%s&customerAccountNumber=%s",
                AppConstants.getBaseUrl(),
                LocalStorage.getInstitutionCode(getBaseContext()),
                //associationDAO.Get(selectedAssociationID).getId(),
                loanRequest_marketAssociations_et.getText().toString().trim(),
                loanRequest_memberID_et.getText().toString().trim(),
                loanRequest_customerAccount_et.getText().toString().trim());
        Log.e("LoanProducts", url);
        showProgressBar("Getting loan products...");
        RequestQueue queue = Volley.newRequestQueue(this);
        //JSONObject convertedObject = null;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        if (response != null) {
                            try {
                                Log.e("LoanProducts", response);
                                //Standard .NET additions to serialized objects
                                response = response.replace("\\", "").replace("\n", "").trim();
                                TypeToken<ArrayList<LoanProduct>> typeToken = new TypeToken<ArrayList<LoanProduct>>() {
                                };
                                eligibleLoanProducts = new Gson().fromJson(response, typeToken.getType());

                                ArrayList<String> loanProductsInfo = new ArrayList<>();
                                loanProductsInfo.add("Select a loan product...");
                                for (LoanProduct loanProduct : eligibleLoanProducts)
                                    loanProductsInfo.add(String.format(Locale.getDefault(),
                                            "%s - (N%s - N%s)",
                                            loanProduct.getName(),
                                            String.valueOf(loanProduct.getMinimumAmount()),
                                            String.valueOf(loanProduct.getMaximumAmount())));
                                Misc.populateSpinnerWithString(CreditClubLoanRequestActivity.this, loanProductsInfo, loanProductsSpinner);
                                hideProgressBar();
                                mViewPager.setCurrentItem(1, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                                showError(e.getMessage());
                            }
                        } else {
                            Log.e("ResponseFailed", "Api call failed");
                            showError("Api call failed");
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LoanProducts", new Gson().toJson(error));
                showError("A network-related error occurred.");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void sendRequestLoanPostRequest(String url, String data) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject convertedObject = null;
        try {
            convertedObject = new JSONObject(data);
        } catch (Exception e) {
            Log.e("creditclub", "failed json parsing");
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, convertedObject, new com.android.volley.Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject object) {
                String result = object.toString();
                result = result.replace("\\", "").replace("\n", "").trim();
                com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
                Response serverResponse = new Gson().fromJson(result, Response.class);
                if (serverResponse.isSuccessful()) {
                    showNotification("Loan request was made successfully", true);
                } else {
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
    }

    public void requestLoan_click(View view) {


        String accountNumber = loanRequest_customerAccount_et.getText().toString().trim();
        if (accountNumber.length() != 10) {
            Toast.makeText(getBaseContext(), "Please enter customer phone number", Toast.LENGTH_LONG).show();
            mViewPager.setCurrentItem(0);
            loanRequest_customerAccount_et.requestFocus();
            return;
        }


        String loanAmount = loanRequest_loanAmount_et.getText().toString().trim();

        if (loanAmount.length() == 0) {
            showError("Please enter a loan amount");
            return;
        }

        try {
            Double.parseDouble(loanAmount);
        } catch (Exception ex) {
            showError("Please enter a numeric amount");
            return;
        }

        String phoneNumber = loanRequest_customerPhone_et.getText().toString();
        if (phoneNumber.length() != 11) {
            showError("Phone number must have 11 digits");
            mViewPager.setCurrentItem(0);
            loanRequest_customerPhone_et.requestFocus();
            return;
        }


        if (loanProductsSpinner.getSelectedItemPosition() == 0) {
            showError("Please select a loan product");
            return;
        }

        if (agentPIN_et.getText().toString().length() == 0) {
            showError("Please enter your PIN");
            return;
        }

        if (agentPIN_et.getText().toString().length() != 4) {
            showError("PIN must be four digits");
            return;
        }


        String agentPhoneNumber = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
        String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

        //String code = associationDAO.Get(selectedAssociationID).getId();

        LoanRequestCreditClub loanRequest = new LoanRequestCreditClub();
        loanRequest.setCustomerAccountNumber(accountNumber);
        loanRequest.setLoanAmount(Double.parseDouble(loanAmount));
        loanRequest.setAgentPhoneNumber(phoneNumber);
        loanRequest.setInstitutionCode(institutionCode);
        //loanRequest.setLoanProductID(productID);
        loanRequest.setAssociationID(loanRequest_marketAssociations_et.getText().toString());
        loanRequest.setMemberID(loanRequest_memberID_et.getText().toString());
        loanRequest.setAgentPhoneNumber(agentPhoneNumber);
        loanRequest.setCustomerAccountNumber(loanRequest_customerAccount_et.getText().toString().trim());
        loanRequest.setInstitutionCode(LocalStorage.getInstitutionCode(getBaseContext()));
        loanRequest.setLoanProductID((int) eligibleLoanProducts.get(loanProductsSpinner.getSelectedItemPosition() - 1).getID());
        loanRequest.setMemberID(loanRequest_memberID_et.getText().toString());
        loanRequest.setAgentPhoneNumber(LocalStorage.getPhoneNumber(getBaseContext()));
        //loanRequest.setAssociationID(associationDAO.Get(selectedAssociationID).getId());
        loanRequest.setGeoLocation(String.format(Locale.getDefault(), "%s:%s", String.valueOf(getGps().getLatitude()), String.valueOf(getGps().getLongitude())));
        loanRequest.setAgentPin(agentPIN_et.getText().toString().trim());

        String data = new Gson().toJson(loanRequest);
        /*loanRequest.setCustomerID(customerID);
        loanRequest.setCustomerName(customerName);
        loanRequest.setBVN(BVN);*/


        showProgressBar("Make Loan Request");
        sendRequestLoanPostRequest(AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/LoanRequest", data);
    }

    public void next_button_click(View view) {

        selectedAssociationID = loanRequest_marketAssociations_et.getText().toString().trim();// adapter.getPosition(associationAutoCompletTV.getText().toString());

        String accountNumber = loanRequest_customerAccount_et.getText().toString().trim();
        if (accountNumber.length() == 0) {
            showError("Please enter the account number");
            return;
        }

        if (accountNumber.length() != 10) {
            showError("Incorrect account number");
            return;
        }

        String amount = loanRequest_loanAmount_et.getText().toString().trim();
        if (amount.length() == 0) {
            showError("Please enter the loan amount");
            return;
        }
        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amount);
        } catch (Exception ex) {
            showError("Please enter a valid amount");
            return;
        }

        if (amountDouble <= 0) {
            showError("Please enter an amount greater than 0");
            return;
        }

        String phoneNumber = loanRequest_customerPhone_et.getText().toString().trim();
        if (phoneNumber.length() == 0) {
            showError("Please enter the customer's phone number");
            return;
        }

        if (phoneNumber.length() != 11) {
            showError("Please enter the correct phone number");
            return;
        }

        if (selectedAssociationID.length() == 0) {
            showError("Please enter the market association ID");
            return;
        }

        if (loanRequest_memberID_et.getText().toString().trim().length() == 0) {
            showError("Please enter the customer's association ID");
            return;
        }

        getLoanProducts();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_loan_request, menu);
        return true;
    }

    /*static void updateAssociationsAutoComplete(String[] associationNames)
    {
        associationAutoCompletTV.setAdapter(new CustomAutoCompleteAdapter(BankOneApplication.getAppContext(), R.layout.item_list, associationNames));

        associationAutoCompletTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                selectedAssociation = null;
                selectedAssociation = (String) adapterView.getItemAtPosition(position);
                associationAutoCompletTV.setText(selectedAssociation);

                adapter = ((CustomAutoCompleteAdapter)
                        associationAutoCompletTV.getAdapter());

            }
        });
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case android.R.id.home:
                if (mViewPager.getCurrentItem() > 0) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                    return true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class FirstFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_loan_request, container, false);

            loanRequest_customerAccount_et = (EditText) rootView.findViewById(R.id.loanRequest_customerAccount_et);
            loanRequest_loanAmount_et = (EditText) rootView.findViewById(R.id.loanRequest_loanAmount_et);
            loanRequest_customerPhone_et = (EditText) rootView.findViewById(R.id.loanRequest_phoneno_et);
            loanRequest_memberID_et = (EditText) rootView.findViewById(R.id.loanRequest_memberID_et);
            //associationAutoCompletTV = rootView.findViewById(R.id.loanRequest_marketAssociations_actv);
            loanRequest_marketAssociations_et = rootView.findViewById(R.id.loanRequest_marketAssociations_et);

            CreditClubLoanRequestActivity loanActivity = (CreditClubLoanRequestActivity) getActivity();
            loanActivity.addValidPhoneNumberListener(loanRequest_customerPhone_et);
            loanRequest_customerAccount_et.setText(loanActivity.getAccountInfo().getNumber());
            loanRequest_customerPhone_et.setText(loanActivity.getAccountInfo().getPhoneNumber());

            return rootView;
        }

    }

    public static class SecondFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_loan_request2, container, false);

            //loanRequest2_customerName_et = (EditText)rootView.findViewById(R.id.loanRequest2_customerName_et);
            //loanRequest2_customerid_et = (EditText)rootView.findViewById(R.id.loanRequest2_customerid_et);
            //loanRequest2_productid_et = (EditText)rootView.findViewById(R.id.loanRequest2_loanproduct_et);
            //loanRequest2_bvn_et = (EditText)rootView.findViewById(R.id.loanRequest2_bvn_et);
            loanProductsSpinner = rootView.findViewById(R.id.loanRequest_loan_product_spinner);
            agentPIN_et = rootView.findViewById(R.id.agent_pin_et);

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
            //return PlaceholderFragment.newInstance(position + 1);
            if (position == 0) {
                return new FirstFragment();
            } else {
                return new SecondFragment();
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CUSTOMER DETAILS";
                case 1:
                    return "LOAN DETAILS";
            }
            return null;
        }
    }


}