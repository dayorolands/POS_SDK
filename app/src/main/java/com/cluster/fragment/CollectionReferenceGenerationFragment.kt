package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cluster.R
import com.cluster.databinding.FragmentCollectionReferenceGenerationBinding
import com.cluster.ui.dataBinding
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.data.request.CollectionPaymentRequest
import com.cluster.core.data.request.CollectionReferenceGenerationRequest
import com.cluster.core.data.request.CollectionValidationCustomFields
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.pos.Platform
import com.cluster.pos.printer.PosPrinter
import com.cluster.receipt.collectionPaymentReceipt
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.util.*

class CollectionReferenceGenerationFragment :
    CreditClubFragment(R.layout.fragment_collection_reference_generation) {

    private val posPrinter: PosPrinter by inject { parametersOf(requireContext(), dialogProvider) }
    private val request = CollectionPaymentRequest()

    private val binding by dataBinding<FragmentCollectionReferenceGenerationBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    private val uniqueReference = UUID.randomUUID().toString()
    private val collectionsService: CollectionsService by retrofitService()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.retrievalReferenceNumber.value.isNullOrBlank()){
            viewModel.retrievalReferenceNumber.value = localStorage.newTransactionReference()
        }
        Log.d("OkHttpClient", "Checking thee accept payment value:::: ${viewModel.acceptPartPayment.value}")

        mainScope.launch {
            if(viewModel.acceptPartPayment.value == true){
                binding.amountInputPay.isEnabled = true
                binding.amountInputPay.value = ""
            }
            else{
                binding.amountInputPay.isEnabled = false
                binding.amountInputPay.setText(viewModel.paymentItemAmount.value!!).toString()
                binding.amountInputPay.value = viewModel.paymentItemAmount.value.toString()
            }
        }

        binding.confirmPaymentButton.setOnClickListener{
            mainScope.launch {
                completePayment()
            }
        }
    }

    private suspend fun completePayment() {
        if (binding.customerNameInput.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a valid customer name")
        } else {
            viewModel.validCustomerName.value = binding.customerNameInput.value
        }

        if (binding.customerEmailInput.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a valid email address")
        } else {
            viewModel.validCustomerEmail.value = binding.customerEmailInput.value
        }

        if (binding.customerPhoneNoInput.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a phone number")
        } else {
            viewModel.customerPhoneNumber.value = binding.customerPhoneNoInput.value
        }

        val amountDouble = binding.amountInputPay.value.toDouble()
        val maximumAmount = viewModel.maximumAmount.value?.toDouble()
        val minimumAmount = viewModel.minimumAmount.value?.toDouble()

        if(amountDouble < minimumAmount!!){
            return dialogProvider.showErrorAndWait("Amount cannot be less ${minimumAmount.toDouble().toCurrencyFormat()}")
        }

        if(amountDouble > maximumAmount!!){
            return dialogProvider.showErrorAndWait("Amount cannot be more than ${maximumAmount.toDouble().toCurrencyFormat()}")
        }

        if (amountDouble == 0.0) return dialogProvider.showErrorAndWait("Amount cannot be zero")

        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.length != 4) return dialogProvider.showError("Agent PIN must be 4 digits long")

        val serializer = CollectionPaymentRequest.Additional.serializer()
        val agent = localStorage.agent
        val additional = CollectionPaymentRequest.Additional().apply {
            agentCode = agent?.agentCode
            terminalId = agent?.terminalID
        }
        request.apply {
            paymentReference = viewModel.paymentReferece.value
            paymentMethod = 1
            agentPin = pin
            channel = "mobile"
            paymentGateway = "web"
            billerItemName = viewModel.paymentItem.value?.name
            billerItemCode = viewModel.paymentItem.value?.code
            customerAcctName = viewModel.validCustomerName.value
            customerName = viewModel.validCustomerName.value
            customerEmail = viewModel.validCustomerEmail.value
            customerPhoneNumber = viewModel.customerPhoneNumber.value
            amount = amountDouble
            geoLocation = localStorage.lastKnownLocation
            billerName = viewModel.billers.value?.name
            billerCode = viewModel.billers.value?.code
            currency = "NGN"
            institutionCode = localStorage.institutionCode
            requestReference = uniqueReference
            retrievalReferenceNumber = viewModel.retrievalReferenceNumber.value
            additionalInformation = Json.encodeToString(serializer, additional)
            deviceNumber = localStorage.deviceNumber
            agentPhoneNumber = localStorage.agentPhone
            applyFee = true
            feeAmount = 0
            feeBearerAccount = "Agent"
            feeSuspenseAccount = localStorage.agentPhone
        }
        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            collectionsService.completePayment(request)
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            dialogProvider.showErrorAndWait("An error occurred. Please try again later")
            activity?.onBackPressed()
            return
        }

        response.date = Instant.now()
        response.collectionPaymentItemName = response.collectionPaymentItemName ?: "${viewModel.paymentItem.value?.name} (${viewModel.paymentItem.value?.code})"
        response.collectionCategoryName = response.collectionCategoryName ?: "${viewModel.billers.value?.name} (${viewModel.billers.value?.code})"

        if (response.isSuccessful == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
            if (Platform.hasPrinter) {
                posPrinter.print(collectionPaymentReceipt(requireContext(), response))
            }
            activity?.onBackPressed()
        } else {
            dialogProvider.showErrorAndWait(response.responseMessage ?: "Error")
        }
    }
}