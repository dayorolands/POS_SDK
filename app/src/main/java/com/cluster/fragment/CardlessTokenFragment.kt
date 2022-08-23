package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cluster.R
import com.cluster.core.data.ClusterObjectBox
import com.cluster.core.data.TRANSACTIONS_CLIENT
import com.cluster.core.data.api.CardlessWithdrawalService
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.*
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.defaultJson
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toString
import com.cluster.databinding.CardlessTokenWithdrawalBinding
import com.cluster.executeTransaction
import com.cluster.receipt.tokenWithdrawalReceipt
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.time.Instant
import java.util.*

class CardlessTokenFragment : CreditClubFragment(R.layout.cardless_token_withdrawal) {
    private val viewModel : CardlessWithdrawalViewModel by viewModels()
    private val binding : CardlessTokenWithdrawalBinding by dataBinding()
    private val cardlessTokenService : CardlessWithdrawalService by retrofitService()
    override val functionId = FunctionIds.CARDLESS_TOKEN
    private val validatingCustomerRequest = ValidatingCustomerRequest()
    private val sendCustomerTokenRequest = SendCustomerTokenRequest()
    private val submitTokenRequest = SubmitTokenRequest()
    private val additionalString = SubmitTokenRequest.Additional()
    private val uniqueReference = UUID.randomUUID().toString()
    private val clusterObjectBox: ClusterObjectBox by inject()
    private val transactionService : StaticService by retrofitService(TRANSACTIONS_CLIENT)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.retrievalReferenceNumber.value.isBlank()){
            viewModel.retrievalReferenceNumber.value = localStorage.newTransactionReference()
        }

        binding.viewModel = viewModel
        binding.toolbar.title = "Token Withdrawal"

        mainScope.launch { loadBanks() }

        viewModel.run {
            bankNameList.bindDropDown(bankName, binding.bankInput){ this@bindDropDown }

            bankName.onChange { newBank ->
                mainScope.launch {
                    if(newBank != null){
                        accountNumber.postValue("")
                        customerName.postValue("")
                        amountString.postValue("")
                    }
                }
            }

            binding.resendTokenBtn.setOnClickListener {
                mainScope.launch {
                    customerToken.postValue("")
                    sendToken()
                }
            }
        }

        binding.accountNumberInput.setEndIconOnClickListener {
            mainScope.launch {
                validatingCustomerInfo()
            }
        }

        binding.sendTokenBtn.setOnClickListener {
            mainScope.launch {
                sendToken()
            }
        }

        binding.confirmTokenBtn.setOnClickListener {
            mainScope.launch {
                confirmToken()
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private inline fun <T> MutableStateFlow<List<T>>.bindDropDown(
        selectedItemLiveData: MutableStateFlow<T?>,
        autoCompleteTextView: AutoCompleteTextView,
        crossinline mapFunction: List<T>.() -> List<Any>,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                this@bindDropDown.collect { list ->
                    val items = list.mapFunction()
                    val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
                    autoCompleteTextView.setAdapter(adapter)
                    autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                            selectedItemLiveData.emit(parent.getItemAtPosition(position) as T)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadBanks() =
        loadDependencies("Banks"){
            val bankNameList = cardlessTokenService.getBanks(
                localStorage.institutionCode
            )
            for (i in bankNameList.data!!){
                viewModel.bankNameList.postValue(bankNameList.data!!)
            }
        }

    private inline fun <T> MutableStateFlow<T>.onChange(crossinline block: (value: T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var oldValue = value
                collect {
                    if (it != oldValue) {
                        oldValue = it
                        block(it)
                    }
                }
            }
        }
    }

    private suspend fun validatingCustomerInfo() = coroutineScope {
        val accountNumberField = viewModel.accountNumber.value
        if(accountNumberField.isBlank()){
            dialogProvider.showError("Account number should not be empty")
            return@coroutineScope
        }

        val maxLength = institutionConfig.bankAccountNumberLength
        if(accountNumberField.length != maxLength){
            dialogProvider.showError("Account number cannot be more than $maxLength")
            return@coroutineScope
        }

        dialogProvider.showProgressBar("Validating customer details", isCancellable = true) {
            onClose {
                cancel()
            }
        }

        validatingCustomerRequest.apply {
            institutionCode = localStorage.institutionCode
            bankCode = viewModel.bankName.value!!.dataCode
            bankCbnCode = viewModel.bankName.value!!.cbnCode
            customerAccountNumber = accountNumberField
        }

        val (response, error) = safeRunIO {
            cardlessTokenService.validatingCustomerRequestInfo(validatingCustomerRequest)
        }

        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return@coroutineScope
        }
        if (response == null) {
            dialogProvider.showErrorAndWait(getString(R.string.an_error_occurred_please_try_again_later))
            return@coroutineScope
        }

        if (!response.isSuccessful) {
            val message = response.message ?: getString(R.string.network_error_message)
            dialogProvider.showErrorAndWait(message)
            return@coroutineScope
        }

        viewModel.customerNameValidationResponse.postValue(response)
        val phoneNumber = response.data?.phoneNumber.let { it } ?: ""
        viewModel.customerPhoneNumber.value = phoneNumber
        binding.customerAccountName.visibility = View.VISIBLE
        binding.customerAccountName.setText(response.data!!.accountName)
        binding.customerAccountName.isEnabled = false
        binding.amountInput.visibility = View.VISIBLE
    }

    private suspend fun sendToken() {
        val accountNumberField = viewModel.accountNumber.value

        val amountEntered = viewModel.amountString.value
        if(amountEntered.isBlank()){
            return dialogProvider.showErrorAndWait("Amount cannot be empty")
        }
        if (amountEntered == "0") return dialogProvider.showErrorAndWait("Amount cannot be zero")

        val customerPhoneNo = viewModel.customerPhoneNumber.value
        val bankCode = viewModel.bankName.value!!.dataCode
        val cbnBankCode = viewModel.bankName.value!!.cbnCode
        sendCustomerTokenRequest.apply {
            institutionCode = localStorage.institutionCode
            destinationBankCode = bankCode.ifEmpty { cbnBankCode }
            customerAccountNumber = accountNumberField
            amount = amountEntered
        }

        dialogProvider.showProgressBar("Sending Token")

        val (response, error) = safeRunIO {
            cardlessTokenService.sendCustomerToken(sendCustomerTokenRequest)
        }

        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return
        }
        if (response == null) {
            dialogProvider.showErrorAndWait("A network-related error occurred while sending token")
            return
        }

        if (!response.isSuccessful) {
            val errorMessage = response.responseMessage ?: "An error occurred while sending token"
            dialogProvider.showErrorAndWait(errorMessage)
            return
        }

        dialogProvider.showSuccess(response.responseMessage)

        binding.sendTokenBtn.visibility = View.GONE
        binding.inquiryLayout.visibility = View.GONE
        binding.sendTokenLayout.visibility = View.VISIBLE
        binding.customerPhoneNumber.text = "Please input the token sent to $customerPhoneNo"
        binding.confirmTokenBtn.visibility = View.VISIBLE
        binding.resendTokenBtn.visibility = View.VISIBLE
    }

    private suspend fun confirmToken() = coroutineScope{
        val token = viewModel.customerToken.value
        if(token.isBlank()){
            dialogProvider.showError("Token cannot be blank")
            return@coroutineScope
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return@coroutineScope
        if (pin.length != 4) {
            dialogProvider.showError("Agent PIN must be 4 digits long")
            return@coroutineScope
        }

        val serializer = SubmitTokenRequest.Additional.serializer()
        val agent = localStorage.agent
        val additional = additionalString.apply {
            agentCode = agent?.agentCode
            terminalId = agent?.terminalID
        }

        val customerName = viewModel.customerName.value
        val accountNumber = viewModel.accountNumber.value
        val bankName = viewModel.bankNames.value

        submitTokenRequest.apply {
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            customerAccountNumber = accountNumber
            amount = viewModel.amountString.value
            agentPin = pin
            customerToken = viewModel.customerToken.value
            geoLocation = localStorage.lastKnownLocation
            retrievalReferenceNumber = viewModel.retrievalReferenceNumber.value
            deviceNumber = localStorage.deviceNumber
            additionalInformation = Json.encodeToString(serializer, additional)
            destinationBankCode = viewModel.bankName.value!!.dataCode
            requestReference = uniqueReference
        }

        dialogProvider.showProgressBar("Processing..", isCancellable = true) {
            onClose {
                cancel()
            }
        }

        val requestTime = Instant.now()
        val pendingTransactionsBox: Box<PendingTransaction> = clusterObjectBox.boxStore.boxFor()
        val pendingTransaction = PendingTransaction(
            transactionType = TransactionType.CrossBankTokenWithdrawal,
            requestJson = defaultJson.encodeToString(
                SubmitTokenRequest.serializer(),
                submitTokenRequest
            ),
            accountName = customerName,
            accountNumber = accountNumber,
            amount = submitTokenRequest.amount!!.toDouble(),
            reference = submitTokenRequest.retrievalReferenceNumber!!,
            createdAt = requestTime,
            lastCheckedAt = null,
            transactionPending = false,
        )

        val (response, error) = executeTransaction(
            fetcher = { cardlessTokenService.confirmToken(submitTokenRequest) },
            reFetcher = { transactionService.getTransactionStatusByReferenceNumber(
                deviceNumber = localStorage.deviceNumber,
                retrievalReferenceNumber = submitTokenRequest.retrievalReferenceNumber!!,
                institutionCode = localStorage.institutionCode,
            ) },
            pendingTransaction = pendingTransaction,
            pendingTransactionsBox = pendingTransactionsBox,
            dialogProvider = dialogProvider,
        )

//        val (response, error) = safeRunIO {
//            cardlessTokenService.confirmToken(submitTokenRequest)
//        }

        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return@coroutineScope
        }
        if (response == null) {
            dialogProvider.showErrorAndWait("A network-related error occurred while confirming token")
            return@coroutineScope
        }

        if (!response.isSuccessful) {
            val errorMessage = response.message ?: "An error occurred while confirming token"
            dialogProvider.showErrorAndWait(errorMessage)
            return@coroutineScope
        }

        val receipt = tokenWithdrawalReceipt(
            context = requireContext(),
            request = submitTokenRequest,
            response = response as SubmitTokenResponse,
            transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm"),
            customerName = customerName,
            bankName = bankName
        )

        navigateToReceipt(receipt, true)
    }

    private suspend inline fun loadDependencies(
        dependencyName: String,
        crossinline fetcher: suspend () -> Unit,
    ) = coroutineScope {
        dialogProvider.showProgressBar("Loading $dependencyName", isCancellable = true) {
            onClose {
                cancel()
            }
        }
        val (items) = safeRunIO { fetcher() }
        dialogProvider.hideProgressBar()

        if (items == null) {
            dialogProvider.showErrorAndWait("An error occurred while loading $dependencyName")
            return@coroutineScope
        }
    }

    private fun <T> MutableStateFlow<T>.postValue(value: T) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            emit(value)
        }
    }
}