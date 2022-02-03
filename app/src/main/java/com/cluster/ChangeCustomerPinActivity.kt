package com.cluster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cluster.databinding.ActivityChangeCustomerPinBinding
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.AccountInfo
import com.cluster.core.data.request.PinChangeRequest
import com.cluster.core.type.TokenType
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.safeRunIO
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class ChangeCustomerPinActivity : CreditClubActivity(R.layout.activity_change_customer_pin) {
    private val binding: ActivityChangeCustomerPinBinding by dataBinding()

    private var oldPin = ""
    private var newPin = ""
    private var confirmNewPin = ""
    private var customerToken = ""
    private var customerAccount = ""
    override val functionId = FunctionIds.CUSTOMER_CHANGE_PIN
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var oldPinET: EditText? = null
    private var newPinET: EditText? = null
    private var confirmNewPinET: EditText? = null
    private var customerTokenET: EditText? = null
    private var customerAccountNoEt: EditText? = null
    private var customerPhoneEt: EditText? = null
    private var submitBtn: Button? = null
    private val staticService: StaticService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSectionsPagerAdapter = SectionsPagerAdapter(
            supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        binding.container.adapter = mSectionsPagerAdapter
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(binding.container)
        tabLayout.clearOnTabSelectedListeners()
        tabLayout.isClickable = false
        binding.container.offscreenPageLimit = mSectionsPagerAdapter!!.count
    }

    fun send_customer_token_click(view: View?) {
        customerAccount = customerAccountNoEt!!.text.toString().trim { it <= ' ' }
        if (customerAccount.isEmpty()) {
            dialogProvider.showError(getString(R.string.please_enter_the_account_number))
            return
        }
        if (customerAccount.length != 10) {
            dialogProvider.showError(getString(R.string.please_enter_the_complete_account_number))
            return
        }
        mainScope.launch {
            sendCustomerToken(customerAccount, customerAccountNoEt!!)
        }
    }

    private suspend fun sendCustomerToken(customerPhoneNumber: String, phoneNumberET: EditText) {
        //make the phone number EditText uneditable while sending the token
        phoneNumberET.isEnabled = false
        val accountInfo = AccountInfo(
            number = customerPhoneNumber
        )
        val status = sendToken(accountInfo = accountInfo, operationType = TokenType.PinChange)
        if (!status) {
            phoneNumberET.run {
                isFocusable = true
                isEnabled = true
                requestFocus()
            }
            return
        }
        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    private suspend fun changePin() {
        oldPin = oldPinET!!.text.toString()
        if (oldPin.length != 4) {
            dialogProvider.indicateError("Please enter the old PIN", oldPinET)
            return
        }
        newPin = newPinET!!.text.toString().trim { it <= ' ' }
        if (newPin.isEmpty()) {
            dialogProvider.indicateError("Please enter your PIN", newPinET)
            return
        }
        confirmNewPin = confirmNewPinET!!.text.toString()
        if (confirmNewPin != newPin) {
            dialogProvider.indicateError(getString(R.string.new_pin_confirmation_mismatch),
                confirmNewPinET)
            return
        }
        if (customerPhoneEt!!.text.toString().trim { it <= ' ' }.length != 11) {
            dialogProvider.indicateError(getString(R.string.please_enter_customers_phone_number),
                customerPhoneEt)
            return
        }
        customerToken = customerTokenET!!.text.toString()
        if (customerToken.isEmpty()) {
            dialogProvider.indicateError("Token cannot be empty", customerTokenET)
            return
        }

        val changePinRequest = PinChangeRequest(
            agentPhoneNumber = localStorage.agentPhone,
            institutionCode = localStorage.institutionCode,
            newPin = newPin,
            confirmNewPin = confirmNewPin,
            oldPin = oldPin,
            geoLocation = localStorage.lastKnownLocation,
            customerPhoneNumber = customerPhoneEt!!.value,
            agentPin = "0000",
            customerToken = customerToken,
        )

        dialogProvider.showProgressBar("Processing")
        val (response, error) = safeRunIO {
            staticService.pinChange(request = changePinRequest)
        }
        dialogProvider.hideProgressBar()

        if (response == null) {
            dialogProvider.showError(error!!)
            return
        }
        if (response.isSuccessful) {
            dialogProvider.showSuccessAndWait("Pin Changed Successfully")
            finish()
        } else {
            dialogProvider.showError(response.responseMessage)
        }
    }

    inner class CustomerAccountNumberFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_account_number, container, false)
            customerAccountNoEt = //(EditText)
                rootView.findViewById(R.id.customer_account_number_et)
            return rootView
        }
    }

    inner class ChangePinFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_pin_details, container, false)
            oldPinET = //(EditText)
                rootView.findViewById(R.id.old_pin_et)
            newPinET = //(EditText)
                rootView.findViewById(R.id.new_pin_et)
            confirmNewPinET = //(EditText)
                rootView.findViewById(R.id.confirm_new_pin_et)
            customerTokenET = //(EditText)
                rootView.findViewById(R.id.customer_token_et)
            customerPhoneEt = rootView.findViewById(R.id.customer_phone_et)
            submitBtn = rootView.findViewById(R.id.submit_btn)
            submitBtn!!.setOnClickListener {
                mainScope.launch {
                    changePin()
                }
            }
            return rootView
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return if (position == 1) {
                ChangePinFragment()
            } else CustomerAccountNumberFragment()
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Account Number"
                1 -> return "Pin Details"
            }
            return null
        }
    }
}