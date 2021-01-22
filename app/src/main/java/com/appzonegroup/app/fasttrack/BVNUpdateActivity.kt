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
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.request.BVNRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.android.synthetic.main.fragment_account_details.view.*
import kotlinx.android.synthetic.main.fragment_authorization.view.*
import kotlinx.coroutines.launch

class BVNUpdateActivity : CustomerBaseActivity() {
    private val binding by contentView<BVNUpdateActivity, ActivityBvnupdateBinding>(R.layout.activity_bvnupdate)
    override val functionId = FunctionIds.BVN_UPDATE

    override fun onCustomerReady(savedInstanceState: Bundle?){
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

    class AuthorizationFragment : CreditClubFragment() {
        val activity get() = getActivity() as BVNUpdateActivity

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_authorization, container, false)

            rootView.accountDetails_bvn_et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 11) activity.mainScope.launch {
                        val staticService = activity.creditClubMiddleWareAPI.staticService
                        activity.showProgressBar("Getting the BVN information...")

                        val (response, error) = safeRunIO {
                            staticService.getCustomerDetailsByBVN(activity.localStorage.institutionCode, "$s")
                        }

                        activity.hideProgressBar()

                        if (error != null) return@launch activity.showError(error)

                        if (response == null) {
                            activity.showError("BVN is invalid")
                            return@launch
                        }

                        rootView.customer_name_et.setText("${response.firstName} ${response.lastName}")
                    }
                }
            })

            rootView.button_update_bvn.setOnClickListener {
                if (rootView.accountDetails_bvn_et.text.toString().length != 11) {
                    activity.errorAction(rootView.accountDetails_bvn_et, "Incorrect BVN inputted", 1)
                    return@setOnClickListener
                }

                if (rootView.auth_agentPIN_et.text.toString().isEmpty()) {
                    activity.errorAction(rootView.auth_agentPIN_et, "Enter your PIN", 1)
                    return@setOnClickListener
                }

                activity.requireAndValidateToken(activity.accountInfo, operationType = TokenType.BVNUpdate) {
                    onSubmit {
                        val staticService = activity.creditClubMiddleWareAPI.staticService

                        val bvnRequest = BVNRequest()

                        bvnRequest.customerAccountNumber = activity.accountInfo.number
                        bvnRequest.customerPhoneNumber = activity.accountInfo.phoneNumber
                        bvnRequest.bvn = rootView.accountDetails_bvn_et.text.toString().trim { it <= ' ' }
                        bvnRequest.geoLocation = activity.gps.geolocationString
                        bvnRequest.institutionCode = activity.localStorage.institutionCode
                        bvnRequest.agentPhoneNumber = activity.localStorage.agentPhone
                        bvnRequest.agentPin = rootView.auth_agentPIN_et.text.toString()

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
                                response.responseMessage ?: getString(R.string.network_error_message)
                            )
                        }
                    }
                }
            }

            return rootView
        }
    }

    class AccountDetailsFragment : CreditClubFragment() {
        val activity get() = getActivity() as BVNUpdateActivity

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_account_details, container, false)

            activity.addValidPhoneNumberListener(rootView.accountDetails_phone_et)

            if (!activity.gps.canGetLocation()) activity.gps.showSettingsAlert()

            return rootView
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
