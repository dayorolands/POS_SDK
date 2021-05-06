package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.appzonegroup.app.fasttrack.databinding.ActivityBvnupdateBinding
import com.appzonegroup.app.fasttrack.databinding.FragmentAuthorizationBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.request.BVNRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.coroutines.launch

class BVNUpdateActivity : CustomerBaseActivity() {
    private val binding by contentView<BVNUpdateActivity, ActivityBvnupdateBinding>(R.layout.activity_bvnupdate)
    override val functionId = FunctionIds.BVN_UPDATE

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        binding.container.adapter = SectionsPagerAdapter(supportFragmentManager)
        binding.tabs.setupWithViewPager(binding.container)
        binding.tabs.clearOnTabSelectedListeners()
        binding.container.setCurrentItem(1, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun errorAction(view: View, message: String, navigationPageIndex: Int) {
        showError(message)
        view.requestFocus()

        binding.container.setCurrentItem(navigationPageIndex, true)
    }

    fun next_button_click(view: View) {

        //        if (str == null){
        //            errorAction(accountDetails_institutionCodeactv, "Please select an institution", 0);
        //            return;
        //        }

//        if (accountDetails_phone_et.text.toString().isEmpty()) {
//            errorAction(accountDetails_phone_et, "Please enter the customer's phone number", 0)
//            return
//        }
//
//        if (accountDetails_phone_et.text.toString().length != 11) {
//            errorAction(accountDetails_phone_et, "Phone number must be 11 digits", 0)
//            return
//        }
//
//        if (!(accountDetails_accountNumber_et.text.toString().length == 10 || accountDetails_accountNumber_et.text.toString().length == 17)) {
//            errorAction(accountDetails_accountNumber_et, "Incorrect customer's account number", 0)
//            return
//        }

        showProgressBar("Fetching customer name...")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    class AuthorizationFragment : CreditClubFragment(R.layout.fragment_authorization) {
        private val binding:FragmentAuthorizationBinding by dataBinding()
        val activity get() = getActivity() as BVNUpdateActivity

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.accountDetailsBvnEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 11) activity.mainScope.launch {
                        val staticService = activity.creditClubMiddleWareAPI.staticService
                        activity.showProgressBar("Getting the BVN information...")

                        val (response, error) = safeRunIO {
                            staticService.getCustomerDetailsByBVN(
                                activity.localStorage.institutionCode,
                                "$s"
                            )
                        }

                        activity.hideProgressBar()

                        if (error != null) return@launch activity.showError(error)

                        if (response == null) {
                            activity.showError("BVN is invalid")
                            return@launch
                        }

                        binding.customerNameEt.setText("${response.firstName} ${response.lastName}")
                    }
                }
            })

            binding.buttonUpdateBvn.setOnClickListener {
                if (binding.accountDetailsBvnEt.text.toString().length != 11) {
                    activity.errorAction(
                        binding.accountDetailsBvnEt,
                        "Incorrect BVN inputted",
                        1
                    )
                    return@setOnClickListener
                }

                if (binding.authAgentPINEt.text.toString().isEmpty()) {
                    activity.errorAction(binding.authAgentPINEt, "Enter your PIN", 1)
                    return@setOnClickListener
                }

                activity.requireAndValidateToken(
                    activity.accountInfo,
                    operationType = TokenType.BVNUpdate
                ) {
                    onSubmit {
                        val staticService = activity.creditClubMiddleWareAPI.staticService

                        val bvnRequest = BVNRequest()

                        bvnRequest.customerAccountNumber = activity.accountInfo.number
                        bvnRequest.customerPhoneNumber = activity.accountInfo.phoneNumber
                        bvnRequest.bvn =
                            binding.accountDetailsBvnEt.text.toString().trim { it <= ' ' }
                        bvnRequest.geoLocation = activity.gps.geolocationString
                        bvnRequest.institutionCode = activity.localStorage.institutionCode
                        bvnRequest.agentPhoneNumber = activity.localStorage.agentPhone
                        bvnRequest.agentPin = binding.authAgentPINEt.text.toString()

                        activity.mainScope.launch {
                            activity.showProgressBar("Updating BVN")

                            val (response, error) = safeRunIO {
                                staticService.bVNUpdate(bvnRequest)
                            }

                            activity.hideProgressBar()

                            if (error.isNetworkError()) return@launch activity.showNetworkError()
                            response ?: return@launch activity.showInternalError()

                            if (response.isSuccessful) {
                                activity.dialogProvider.showSuccess("BVN was updated successfully") {
                                    onClose {
                                        activity.finish()
                                    }
                                }
                            } else activity.showError(
                                response.responseMessage
                                    ?: getString(R.string.network_error_message)
                            )
                        }
                    }
                }
            }
        }
    }

    class AccountDetailsFragment : CreditClubFragment(R.layout.fragment_account_details) {
        val activity get() = getActivity() as BVNUpdateActivity

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            activity.addValidPhoneNumberListener(view.findViewById(R.id.accountDetails_phone_et))
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return if (position % 2 == 0) {
                AccountDetailsFragment()
            } else {
                AuthorizationFragment()
            }
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Account Details"
                1 -> return "Authentication"
            }
            return null
        }
    }
}
