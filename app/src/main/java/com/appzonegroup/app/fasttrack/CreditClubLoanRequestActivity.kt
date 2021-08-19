package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.appzonegroup.app.fasttrack.model.LoanProduct
import com.appzonegroup.app.fasttrack.model.LoanRequestCreditClub
import com.appzonegroup.app.fasttrack.model.Response
import com.appzonegroup.app.fasttrack.utility.CustomAutoCompleteAdapter
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.app.fasttrack.utility.Misc
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.util.*

class CreditClubLoanRequestActivity : CustomerBaseActivity() {
    var eligibleLoanProducts: ArrayList<LoanProduct>? = null
    override val functionId: Int = FunctionIds.LOAN_REQUEST

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    private var loanRequest_customerPhone_et: EditText? = null

    //loanRequest2_customerName_et,
    private var loanRequest_marketAssociations_et: EditText? = null
    private var loanRequest_memberID_et: EditText? = null
    private  var loanRequest_customerAccount_et: EditText? = null
    private  var loanRequest_loanAmount_et: EditText? = null

    //loanRequest2_bvn_et,
    //loanRequest2_productid_et,
    //loanRequest2_customerid_et,
    private var agentPIN_et: EditText? = null
    private var loanProductsSpinner: Spinner? = null
    private var adapter: CustomAutoCompleteAdapter? = null

    //public AssociationList associationList;
    //private static String selectedAssociation;
    private var selectedAssociationID = ""

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_loan_request)
        mSectionsPagerAdapter = SectionsPagerAdapter(
            supportFragmentManager
        )
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
        tabLayout.clearOnTabSelectedListeners()
    }

    override fun onBackPressed() {
        if (mViewPager!!.currentItem > 0) {
            mViewPager!!.setCurrentItem(mViewPager!!.currentItem - 1, true)
        } else {
            super.onBackPressed()
        }
    }

    private val loanProducts: Unit
        get() {
            val url = String.format(
                Locale.getDefault(),
                "%s/CreditClubMiddleWareAPI/CreditClubStatic/GetEligibleLoanProducts?institutionCode=%s&associationID=%s&memberID=%s&customerAccountNumber=%s",
                BuildConfig.API_HOST,
                localStorage.institutionCode,  //associationDAO.Get(selectedAssociationID).getId(),
                loanRequest_marketAssociations_et!!.text.toString().trim { it <= ' ' },
                loanRequest_memberID_et!!.text.toString().trim { it <= ' ' },
                loanRequest_customerAccount_et!!.text.toString().trim { it <= ' ' })
            Log.e("LoanProducts", url)
            showProgressBar("Getting loan products...")
            val queue = Volley.newRequestQueue(this)
            //JSONObject convertedObject = null;
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    // Display the first 500 characters of the response string.
                    var response = response
                    if (response != null) {
                        try {
                            Log.e("LoanProducts", response)
                            //Standard .NET additions to serialized objects
                            response =
                                response.replace("\\", "").replace("\n", "").trim { it <= ' ' }
                            val typeToken = object : TypeToken<ArrayList<LoanProduct?>?>() {}
                            eligibleLoanProducts = Gson().fromJson(response, typeToken.type)
                            val loanProductsInfo = ArrayList<String>()
                            loanProductsInfo.add("Select a loan product...")
                            eligibleLoanProducts?.forEach { loanProduct ->
                                loanProductsInfo.add(
                                    String.format(
                                        Locale.getDefault(),
                                        "%s - (N%s - N%s)",
                                        loanProduct.name,
                                        loanProduct.minimumAmount.toString(),
                                        loanProduct.maximumAmount.toString()
                                    )
                                )
                            }
                            Misc.populateSpinnerWithString(
                                this@CreditClubLoanRequestActivity,
                                loanProductsInfo,
                                loanProductsSpinner
                            )
                            hideProgressBar()
                            mViewPager!!.setCurrentItem(1, true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrashlytics.getInstance().recordException(Exception(e.message))
                            showError(e.message)
                        }
                    } else {
                        Log.e("ResponseFailed", "Api call failed")
                        showError("Api call failed")
                    }
                }) { error ->
                Log.e("LoanProducts", Gson().toJson(error))
                showError("A network-related error occurred.")
            }

// Add the request to the RequestQueue.
            queue.add(stringRequest)
        }

    private fun sendRequestLoanPostRequest(url: String?, data: String?) {
        val queue = Volley.newRequestQueue(this)
        var convertedObject: JSONObject? = null
        try {
            convertedObject = JSONObject(data)
        } catch (e: Exception) {
            Log.e("creditclub", "failed json parsing")
        }
        val request = JsonObjectRequest(Request.Method.POST, url, convertedObject, { `object` ->
            var result = `object`.toString()
            result = result.replace("\\", "").replace("\n", "").trim { it <= ' ' }
            val response = Gson().fromJson(result, Response::class.java)
            val serverResponse = Gson().fromJson(result, Response::class.java)
            if (serverResponse.isSuccessful) {
                dialogProvider.showSuccess("Loan request was made successfully") {
                    onClose {
                        finish()
                    }
                }
            } else {
                showError(response.reponseMessage)
            }
        }) { showError("A network-related error just occurred. Please try again later") }
        queue.add(request)
    }

    fun requestLoan_click(view: View?) {
        val accountNumber = loanRequest_customerAccount_et!!.text.toString().trim { it <= ' ' }
        if (accountNumber.length != 10) {
            Toast.makeText(baseContext, "Please enter customer phone number", Toast.LENGTH_LONG)
                .show()
            mViewPager!!.currentItem = 0
            loanRequest_customerAccount_et!!.requestFocus()
            return
        }
        val loanAmount = loanRequest_loanAmount_et!!.text.toString().trim { it <= ' ' }
        if (loanAmount.length == 0) {
            showError("Please enter a loan amount")
            return
        }
        try {
            loanAmount.toDouble()
        } catch (ex: Exception) {
            showError("Please enter a numeric amount")
            return
        }
        val phoneNumber = loanRequest_customerPhone_et!!.text.toString()
        if (phoneNumber.length != 11) {
            showError("Phone number must have 11 digits")
            mViewPager!!.currentItem = 0
            loanRequest_customerPhone_et!!.requestFocus()
            return
        }
        if (loanProductsSpinner!!.selectedItemPosition == 0) {
            showError("Please select a loan product")
            return
        }
        if (agentPIN_et!!.text.toString().length == 0) {
            showError("Please enter your PIN")
            return
        }
        if (agentPIN_et!!.text.toString().length != 4) {
            showError("PIN must be four digits")
            return
        }
        val agentPhoneNumber = localStorage.agentPhone
        val institutionCode = localStorage.institutionCode

        //String code = associationDAO.Get(selectedAssociationID).getId();
        val loanRequest = LoanRequestCreditClub()
        loanRequest.customerAccountNumber = accountNumber
        loanRequest.loanAmount = loanAmount.toDouble()
        loanRequest.agentPhoneNumber = phoneNumber
        loanRequest.institutionCode = institutionCode
        //loanRequest.setLoanProductID(productID);
        loanRequest.associationID = loanRequest_marketAssociations_et!!.text.toString()
        loanRequest.memberID = loanRequest_memberID_et!!.text.toString()
        loanRequest.agentPhoneNumber = agentPhoneNumber
        loanRequest.customerAccountNumber =
            loanRequest_customerAccount_et!!.text.toString().trim { it <= ' ' }
        loanRequest.institutionCode = localStorage.institutionCode
        loanRequest.loanProductID =
            eligibleLoanProducts!![loanProductsSpinner!!.selectedItemPosition - 1].id
                .toInt()
        loanRequest.memberID = loanRequest_memberID_et!!.text.toString()
        loanRequest.agentPhoneNumber = localStorage.agentPhone
        //loanRequest.setAssociationID(associationDAO.Get(selectedAssociationID).getId());
        loanRequest.geoLocation = localStorage.lastKnownLocation
        loanRequest.agentPin = agentPIN_et!!.text.toString().trim { it <= ' ' }
        val data = Gson().toJson(loanRequest)
        /*loanRequest.setCustomerID(customerID);
        loanRequest.setCustomerName(customerName);
        loanRequest.setBVN(BVN);*/showProgressBar("Make Loan Request")
        sendRequestLoanPostRequest(
            BuildConfig.API_HOST + "/CreditClubMiddleWareAPI/CreditClubStatic/LoanRequest",
            data
        )
    }

    fun next_button_click(view: View?) {
        selectedAssociationID = loanRequest_marketAssociations_et!!.text.toString()
            .trim { it <= ' ' } // adapter.getPosition(associationAutoCompletTV.getText().toString());
        val accountNumber = loanRequest_customerAccount_et!!.text.toString().trim { it <= ' ' }
        if (accountNumber.length == 0) {
            showError("Please enter the account number")
            return
        }
        if (accountNumber.length != 10) {
            showError("Incorrect account number")
            return
        }
        val amount = loanRequest_loanAmount_et!!.text.toString().trim { it <= ' ' }
        if (amount.length == 0) {
            showError("Please enter the loan amount")
            return
        }
        val amountDouble: Double
        amountDouble = try {
            amount.toDouble()
        } catch (ex: Exception) {
            showError("Please enter a valid amount")
            return
        }
        if (amountDouble <= 0) {
            showError("Please enter an amount greater than 0")
            return
        }
        val phoneNumber = loanRequest_customerPhone_et!!.text.toString().trim { it <= ' ' }
        if (phoneNumber.length == 0) {
            showError("Please enter the customer's phone number")
            return
        }
        if (phoneNumber.length != 11) {
            showError("Please enter the correct phone number")
            return
        }
        if (selectedAssociationID.length == 0) {
            showError("Please enter the market association ID")
            return
        }
        if (loanRequest_memberID_et!!.text.toString().trim { it <= ' ' }.length == 0) {
            showError("Please enter the customer's association ID")
            return
        }
        loanProducts
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_loan_request, menu);
        return true
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            android.R.id.home -> if (mViewPager!!.currentItem > 0) {
                mViewPager!!.setCurrentItem(mViewPager!!.currentItem - 1, true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FirstFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_loan_request, container, false)
            loanRequest_customerAccount_et =
                rootView.findViewById<View>(R.id.loanRequest_customerAccount_et) as EditText
            loanRequest_loanAmount_et =
                rootView.findViewById<View>(R.id.loanRequest_loanAmount_et) as EditText
            loanRequest_customerPhone_et =
                rootView.findViewById<View>(R.id.loanRequest_phoneno_et) as EditText
            loanRequest_memberID_et =
                rootView.findViewById<View>(R.id.loanRequest_memberID_et) as EditText
            //associationAutoCompletTV = rootView.findViewById(R.id.loanRequest_marketAssociations_actv);
            loanRequest_marketAssociations_et =
                rootView.findViewById(R.id.loanRequest_marketAssociations_et)
            val loanActivity = activity as CreditClubLoanRequestActivity?
            loanActivity!!.addValidPhoneNumberListener(loanRequest_customerPhone_et!!)
            loanRequest_customerAccount_et!!.setText(
                loanActivity.accountInfo.number
            )
            loanRequest_customerPhone_et!!.setText(
                loanActivity.accountInfo.phoneNumber
            )
            return rootView
        }
    }

    inner class SecondFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_loan_request2, container, false)

            //loanRequest2_customerName_et = (EditText)rootView.findViewById(R.id.loanRequest2_customerName_et);
            //loanRequest2_customerid_et = (EditText)rootView.findViewById(R.id.loanRequest2_customerid_et);
            //loanRequest2_productid_et = (EditText)rootView.findViewById(R.id.loanRequest2_loanproduct_et);
            //loanRequest2_bvn_et = (EditText)rootView.findViewById(R.id.loanRequest2_bvn_et);
            loanProductsSpinner = rootView.findViewById(R.id.loanRequest_loan_product_spinner)
            agentPIN_et = rootView.findViewById(R.id.agent_pin_et)
            return rootView
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!
    ) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            return if (position == 0) {
                FirstFragment()
            } else {
                SecondFragment()
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "CUSTOMER DETAILS"
                1 -> return "LOAN DETAILS"
            }
            return null
        }
    }
}