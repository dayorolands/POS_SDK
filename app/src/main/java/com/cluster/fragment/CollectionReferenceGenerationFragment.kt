package com.cluster.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navGraphViewModels
import com.cluster.R
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.data.request.CollectionPaymentRequest
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.isValidEmail
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.core.util.toString
import com.cluster.databinding.FragmentCollectionReferenceGenerationBinding
import com.cluster.pos.printer.PosPrinter
import com.cluster.receipt.collectionPaymentReceipt
import com.cluster.ui.dataBinding
import kotlinx.coroutines.launch
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

        mainScope.launch {
            if(!viewModel.customerName.value?.isBlank()!!){
                binding.customerNameInput.isEnabled = false
                binding.customerNameInput.setText(viewModel.customerName.value)
                binding.customerNameInput.value = viewModel.customerName.value.toString()
            }
            else{
                binding.customerNameInput.isEnabled = true
                binding.customerNameInput.value = ""
            }

            if(viewModel.isFixedAmountCheck.value == false){
                binding.amountInputPay.isEnabled = true
                binding.amountInputPay.value = ""
            }
            else{
                binding.amountInputPay.isEnabled = false
                binding.amountInputPay.setText(viewModel.paymentItemAmount.value!!).toString()
                binding.amountInputPay.value = viewModel.paymentItemAmount.value.toString()
            }

//            if(viewModel.feeAmount.value?.toInt() != null){
//                binding.feeInputPay.isEnabled = false
//                binding.feeInputPay.setText(viewModel.feeAmount.value.toString())
//                binding.feeInputPay.value = viewModel.feeAmount.value.toString()
//            }
//            else{
//                binding.feeInputPay.isEnabled = true
//                binding.feeInputPay.value = ""
//            }

            if(viewModel.amountDue.value?.toInt() != null){
                binding.amountDueInput.isEnabled = false
                binding.amountDueInput.setText(viewModel.amountDue.value.toString())
            }
            else{
                binding.amountDueInput.isEnabled =  true
                binding.amountDueInput.value = ""
            }
        }

        binding.confirmPaymentButton.setOnClickListener{
            mainScope.launch {
                completePayment()
            }
        }
    }

    private suspend fun getTransactionFee(amountDouble: Double, institutionCode: String, agentPhoneNumber: String){
        dialogProvider.showProgressBar("Getting Fee")
        val (response, error) = safeRunIO {
            collectionsService.getTransactionFee(
                amount = amountDouble,
                institutionCode = institutionCode,
                agentPhoneNumber = agentPhoneNumber
            )
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            return dialogProvider.showErrorAndWait("An error occurred. Please try again later")
        }

        if(response.isSuccessful!!){
            mainScope.launch {
                dialogProvider.showInfo("The fee for this transaction is ${response.result!!.toCurrencyFormat()}")
            }
            viewModel.feeAmount.value = response.result!!.toInt()
        } else{
            dialogProvider.showErrorAndWait("${response.responseMessage}")
            return
        }
    }

    private suspend fun completePayment() {
        if (binding.customerNameInput.value.isBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a valid customer name")
        } else {
            viewModel.validCustomerName.value = binding.customerNameInput.value
        }

        viewModel.validCustomerEmail.value = binding.customerEmailInput.value
        val customerEmailAddress = viewModel.validCustomerEmail.value
        if (customerEmailAddress!!.isBlank()) { return dialogProvider.showErrorAndWait("Please enter a valid email address") }
        if (customerEmailAddress.isNotBlank() && !customerEmailAddress.isValidEmail()){
            return dialogProvider.showErrorAndWait("Customer Email is invalid")
        }

        viewModel.customerPhoneNumber.value = binding.customerPhoneNoInput.value
        val customerPhoneNo = viewModel.customerPhoneNumber.value
        if (customerPhoneNo!!.isBlank()) { return dialogProvider.showErrorAndWait("Please enter a phone number") }
        if(customerPhoneNo.length != 11){ return dialogProvider.showErrorAndWait("Customer Phone number must be 11 digits") }


        val amountValue = binding.amountInputPay.value
        if(amountValue.isBlank() || amountValue.isEmpty()) return dialogProvider.showErrorAndWait("Amount cannot be Blank")
        val amountDouble = binding.amountInputPay.value.toDouble()
        if (amountDouble == 0.0) return dialogProvider.showErrorAndWait("Amount cannot be zero")
        val amountDue = viewModel.amountDue.value?.toDouble()

        if(amountDouble > amountDue!!){
            return dialogProvider.showErrorAndWait("Amount cannot be more than ${amountDue.toDouble().toCurrencyFormat()}")
        }

        var agentPhoneNo = localStorage.agentPhone!!
        var instituteCode = localStorage.institutionCode!!

        getTransactionFee(amountDouble, instituteCode, agentPhoneNo)

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
            customerEmail = customerEmailAddress
            customerPhoneNumber = customerPhoneNo
            amount = amountDouble
            geoLocation = localStorage.lastKnownLocation
            billerName = viewModel.billers.value?.name
            billerCode = viewModel.billers.value?.code
            currency = "NGN"
            institutionCode = instituteCode
            requestReference = uniqueReference
            retrievalReferenceNumber = viewModel.retrievalReferenceNumber.value
            additionalInformation = Json.encodeToString(serializer, additional)
            deviceNumber = localStorage.deviceNumber
            agentPhoneNumber = agentPhoneNo
            applyFee = true
            feeAmount = viewModel.feeAmount.value
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
           return dialogProvider.showErrorAndWait("An error occurred. Please try again later")
        }

        response.collectionPaymentItemName = response.collectionPaymentItemName ?: "${viewModel.paymentItem.value?.name} (${viewModel.paymentItem.value?.code})"
        response.collectionCategoryName = response.collectionCategoryName ?: "${viewModel.billers.value?.name} (${viewModel.billers.value?.code})"

        val receipt = collectionPaymentReceipt(
            context = requireContext(),
            response = response,
            request = request,
            transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm")
        )
        navigateToCollectionsReceipt(receipt, popBackStack = true)

    }
}