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
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.data.request.CollectionPaymentRequest
import com.cluster.core.data.request.CollectionReferenceGenerationRequest
import com.cluster.core.data.request.CollectionValidationCustomFields
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.safeRunIO
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
    private val customerValidationRequest = CollectionCustomerValidationRequest()

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
        Log.d("OkHttpClient", "Checking the amount Input for the transaction:::: ${viewModel.paymentItemAmount.value!!}")


        ///TODO Please check to ensure that this part of the app doesn't crash
        binding.amountInputPay.setText(viewModel.paymentItemAmount.value!!).toString()

        binding.confirmPaymentButton.setOnClickListener{
            mainScope.launch {
                completePayment()
            }
        }
    }

    private suspend fun validateCustomer(){
        if(binding.customerNameInput.value.isNullOrBlank()){
            return dialogProvider.showErrorAndWait("Please enter a valid customer name")
        } else{
            viewModel.validCustomerName.value = binding.customerNameInput.value
        }

        if (binding.customerEmailInput.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a valid email address")
        } else{
            viewModel.validCustomerEmail.value = binding.customerEmailInput.value
        }

        if (binding.customerPhoneNoInput.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a phone number")
        }else{
            viewModel.customerPhoneNumber.value = binding.customerPhoneNoInput.value
        }

        if (binding.customerValueInput.value.isNullOrBlank()) {
            dialogProvider.showError("Please enter a valid customer value")
        } else{
            viewModel.customerValue.value = binding.customerValueInput.value
        }

        val serializer = CollectionCustomerValidationRequest.CustomFields.serializer()
        val customFieldRequest = CollectionCustomerValidationRequest.CustomFields().apply {
            id = viewModel.paymentItem.value?.id?.toInt()
            name = "Virtual Accounts"
            value = viewModel.customerValue.value
        }
        customerValidationRequest.apply{
            channel = "mobile"
            itemCode = viewModel.paymentItem.value?.code
            amount = viewModel.paymentItemAmount.value?.toDoubleOrNull()
            customFields = Json.encodeToString(serializer, customFieldRequest)
            customerPhoneNumber = viewModel.customerPhoneNumber.value
            customerName = viewModel.validCustomerName.value
            customerEmail = viewModel.validCustomerEmail.value
            institutionCode = localStorage.institutionCode
        }

        dialogProvider.showProgressBar("Validating customer")
        val (response, error) = safeRunIO {
            collectionsService.validateCustomer(customerValidationRequest)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("An error occurred while generating reference")

        if (response.isSuccessful == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
            viewModel.paymentReference.value = response.paymentReference.toString()
            viewModel.surchargeFee.value = response.surcharge?.toInt()
        } else {
            dialogProvider.showError(response.responseMessage ?: "Error")
            return
        }
    }

    private suspend fun completePayment() {
        mainScope.launch {
            validateCustomer()
        }
        val amountDouble = viewModel.paymentItemAmount.value?.toDouble()
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
            paymentReference = viewModel.paymentReference.value
            paymentMethod = 0
            agentPin = pin
            channel = "mobile"
            paymentGateway = "web"
            categoryCode = viewModel.billers.value?.code
            collectionType = viewModel.paymentItem.value?.name
            itemCode = viewModel.paymentItem.value?.code
            customerAcctName = viewModel.customerValue.value
            customerName = viewModel.customerName.value
            customerEmail = viewModel.validCustomerEmail.value
            customerPhoneNumber = viewModel.customerPhoneNumber.value
            amount = amountDouble
            geoLocation = localStorage.lastKnownLocation
            billerId = viewModel.billerId.value
            currency = "NGN"
            institutionCode = localStorage.institutionCode
            collectionService = viewModel.collectionService.value
            requestReference = uniqueReference
            retrievalReferenceNumber = viewModel.retrievalReferenceNumber.value
            additionalInformation = Json.encodeToString(serializer, additional)
            deviceNumber = localStorage.deviceNumber
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