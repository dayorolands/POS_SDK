package com.cluster

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cluster.utility.FunctionIds
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.LoanProduct
import com.creditclub.core.data.model.LoanRequestCreditClub
import com.creditclub.core.util.safeRunIO
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.*

class CreditClubLoanRequestActivity : CustomerBaseActivity() {
    private lateinit var eligibleLoanProducts: List<LoanProduct>
    override val functionId: Int = FunctionIds.LOAN_REQUEST

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    private var loanRequest_customerPhone_et: EditText? = null

    private var loanRequest_marketAssociations_et: EditText? = null
    private var loanRequest_memberID_et: EditText? = null
    private var loanRequest_customerAccount_et: EditText? = null
    private var loanRequest_loanAmount_et: EditText? = null

    private var agentPIN_et: EditText? = null
    private var loanProductsSpinner: Spinner? = null
    private val staticService: StaticService by retrofitService()

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

    private suspend fun getLoanProducts() {
        dialogProvider.showProgressBar("Getting loan products")
        val (response, error) = safeRunIO {
            staticService.getEligibleLoanProducts(
                institutionCode = localStorage.institutionCode!!,
                associationID = loanRequest_marketAssociations_et!!.value,
                memberID = loanRequest_memberID_et!!.value,
                customerAccountNumber = loanRequest_customerAccount_et!!.value,
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showError(error)
            return
        }

        eligibleLoanProducts = response!!
        val loanProductsInfo = mutableListOf("Select a loan product...")
        eligibleLoanProducts.forEach { loanProduct ->
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
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            loanProductsInfo
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        loanProductsSpinner!!.adapter = arrayAdapter

        mViewPager!!.setCurrentItem(1, true)
    }

    private suspend fun sendRequestLoanPostRequest(request: LoanRequestCreditClub) {
        dialogProvider.showProgressBar("Making Loan Request")
        val (response, error) = safeRunIO {
            staticService.loanRequest(request = request)
        }
        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showError(error)
            return
        }

        if (response!!.isSuccessful) {
            dialogProvider.showSuccessAndWait("Loan request was made successfully")
            finish()
        } else {
            dialogProvider.showError(response.responseMessage)
        }
    }

    fun requestLoan_click(view: View?) {
        val accountNumber = loanRequest_customerAccount_et!!.value
        if (accountNumber.length != 10) {
            Toast.makeText(baseContext, "Please enter customer phone number", Toast.LENGTH_LONG)
                .show()
            mViewPager!!.currentItem = 0
            loanRequest_customerAccount_et!!.requestFocus()
            return
        }
        val loanAmount = loanRequest_loanAmount_et!!.text.toString().trim { it <= ' ' }
        if (loanAmount.isEmpty()) {
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
        if (agentPIN_et!!.text.toString().isEmpty()) {
            showError("Please enter your PIN")
            return
        }
        if (agentPIN_et!!.text.toString().length != 4) {
            showError("PIN must be four digits")
            return
        }

        val eligibleLoanProductIndex = loanProductsSpinner!!.selectedItemPosition - 1
        val loanProductId = eligibleLoanProducts[eligibleLoanProductIndex].id.toInt()
        val loanRequest = LoanRequestCreditClub(
            customerAccountNumber = accountNumber,
            loanAmount = loanAmount.toDouble(),
            associationID = loanRequest_marketAssociations_et!!.value,
            memberID = loanRequest_memberID_et!!.value,
            institutionCode = localStorage.institutionCode,
            loanProductID = loanProductId,
            agentPhoneNumber = localStorage.agentPhone,
            geoLocation = localStorage.lastKnownLocation,
            agentPin = agentPIN_et!!.text.toString().trim { it <= ' ' },
        )
        mainScope.launch {
            sendRequestLoanPostRequest(loanRequest)
        }
    }

    fun next_button_click(view: View?) {
        selectedAssociationID = loanRequest_marketAssociations_et!!.text.toString()
            .trim { it <= ' ' } // adapter.getPosition(associationAutoCompletTV.getText().toString());
        val accountNumber = loanRequest_customerAccount_et!!.text.toString().trim { it <= ' ' }
        if (accountNumber.isEmpty()) {
            showError("Please enter the account number")
            return
        }
        if (accountNumber.length != 10) {
            showError("Incorrect account number")
            return
        }
        val amount = loanRequest_loanAmount_et!!.text.toString().trim { it <= ' ' }
        if (amount.isEmpty()) {
            showError("Please enter the loan amount")
            return
        }
        val amountDouble: Double = try {
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
        if (phoneNumber.isEmpty()) {
            showError("Please enter the customer's phone number")
            return
        }
        if (phoneNumber.length != 11) {
            showError("Please enter the correct phone number")
            return
        }
        if (selectedAssociationID.isEmpty()) {
            showError("Please enter the market association ID")
            return
        }
        if (loanRequest_memberID_et!!.value.isBlank()) {
            showError("Please enter the customer's association ID")
            return
        }
        mainScope.launch {
            getLoanProducts()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_loan_request, menu);
        return true
    }

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
            savedInstanceState: Bundle?,
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
            savedInstanceState: Bundle?,
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