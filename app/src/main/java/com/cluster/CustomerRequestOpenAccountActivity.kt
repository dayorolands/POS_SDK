package com.cluster

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cluster.databinding.ActivityOpenAccountBinding
import com.cluster.fragment.*
import com.cluster.receipt.newAccountReceipt
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import com.cluster.utility.Misc
import com.cluster.pos.Platform
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.request.CustomerRequest
import com.cluster.core.data.response.BackendResponse
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.hasOccurred
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.showNetworkError
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.printer.PrinterStatus
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*

class CustomerRequestOpenAccountActivity : CreditClubActivity(R.layout.activity_open_account) {
    private val binding by dataBinding<ActivityOpenAccountBinding>()
    private val printer: PosPrinter by inject { parametersOf(this, dialogProvider) }
    private val viewModel by viewModels<OpenAccountViewModel>()
    private val request by lazy { CustomerRequest().apply { uniqueReferenceID = Misc.getGUID() } }
    private val staticService: StaticService by retrofitService()

    override val functionId = FunctionIds.ACCOUNT_OPENING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = SectionsPagerAdapter(supportFragmentManager)

        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)
        binding.tabs.clearOnTabSelectedListeners()
        binding.container.offscreenPageLimit = adapter.count

        viewModel.requiresEmail.value = true

        viewModel.afterAccountInfo.postValue {
            binding.container.setCurrentItem(
                binding.container.currentItem + 1,
                true
            )
        }

        viewModel.afterGeneralInfo.postValue {
            binding.container.setCurrentItem(
                binding.container.currentItem + 1,
                true
            )
        }

        viewModel.afterDocumentUpload.postValue {
            binding.container.setCurrentItem(
                binding.container.currentItem + 1,
                true
            )
        }

        viewModel.afterAgentPin.postValue {
            mainScope.launch { createCustomer() }
        }
    }

    private suspend fun createCustomer() {
        val gender = viewModel.gender.value

        request.customerLastName = viewModel.surname.value?.trim()
        request.customerFirstName = viewModel.firstName.value?.trim()
        request.dateOfBirth = viewModel.dob.value
        request.placeOfBirth = viewModel.placeOfBirth.value?.trim()
        request.customerPhoneNumber = viewModel.phoneNumber.value?.trim()
        request.gender = gender?.substring(0, 1)?.toLowerCase(Locale.getDefault())
        request.geoLocation = localStorage.lastKnownLocation
        request.starterPackNumber = viewModel.starterPackNo.value?.trim()
        request.address = viewModel.address.value?.trim()
        request.productCode = viewModel.productCode.value
        request.productName = viewModel.productName.value
        request.bvn = viewModel.bvn.value
        request.agentPhoneNumber = localStorage.agentPhone
        request.agentPin = viewModel.agentPIN.value
        request.institutionCode = localStorage.institutionCode

        val additionalInformation = CustomerRequest.Additional()
        additionalInformation.passport = viewModel.passportString.value
        additionalInformation.email = viewModel.email.value?.trim()
        additionalInformation.middleName = viewModel.middleName.value?.trim()
        additionalInformation.title = if (gender == "female") "Ms" else "Mr"
        additionalInformation.country = "NGN"
        additionalInformation.state = viewModel.addressState.value?.name
        additionalInformation.lga = viewModel.addressLga.value?.name
        additionalInformation.referralCode = viewModel.referralCode.value
        additionalInformation.referralAccountNo = viewModel.referralAcctNo.value

        //additionalInformation.setIDCard(idCardString);
        //additionalInformation.setOccupation(occupation);

        //additionalInformation.setSignature(signatureString);
        //additionalInformation.setProvince(province);

        request.additionalInformation =
            Json.encodeToString(
                CustomerRequest.Additional.serializer(),
                additionalInformation
            )

        dialogProvider.showProgressBar("Creating customer account")
        val (response, error) = safeRunIO {
            staticService.register(request)
        }
        dialogProvider.hideProgressBar()

        if (error.hasOccurred) return dialogProvider.showError(error!!)
        response ?: return dialogProvider.showNetworkError()

        if (response.isSuccessful) {
            dialogProvider.showSuccess(getString(R.string.customer_was_created_successfully)) {
                onClose {
                    finish()
                }
            }
        } else {
            dialogProvider.showError(
                response.responseMessage
                    ?: getString(R.string.an_error_occurred_please_try_again_later)
            )
        }

        if (Platform.hasPrinter) {
            printReceipt(response)
        }
    }

    private fun printReceipt(response: BackendResponse) {
        var accountName: String? = null
        var accountNumber: String? = null
        if (response.isSuccessful) {
            accountName =
                "${request.customerFirstName} ${viewModel.middleName.value} ${request.customerLastName}"

            response.responseMessage?.run {
                accountNumber = this
            }
        }
        val receipt = newAccountReceipt(
            context = this,
            isSuccessful = response.isSuccessful,
            reason = response.responseMessage,
            bvn = request.bvn,
            uniqueReferenceID = request.uniqueReferenceID!!,
            accountName = accountName,
            accountNumber = accountNumber,
        )

        printer.printAsync(receipt) { printerStatus ->
            if (printerStatus != PrinterStatus.READY) dialogProvider.showError(printerStatus.message)
        }
    }

    override fun onBackPressed() {
        if (binding.container.currentItem > 0) {
            binding.container.setCurrentItem(binding.container.currentItem - 1, true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            android.R.id.home -> if (binding.container.currentItem > 0) {
                binding.container.setCurrentItem(binding.container.currentItem - 1, true)
                true
            } else {
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AccountInfoFragment()
                1 -> GeneralInfoFragment()
                2 -> DocumentUploadFragment()
                3 -> AgentPINFragment()
                else -> AccountInfoFragment()
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Account Info"
                1 -> return "General Details"
                2 -> return "Document Upload"
                3 -> return "Agent PIN"
            }
            return null
        }
    }
}

