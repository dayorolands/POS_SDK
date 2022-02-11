package com.cluster.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cluster.R
import com.cluster.core.data.ClusterObjectBox
import com.cluster.core.data.api.BillsPaymentService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.BillCategory
import com.cluster.core.data.model.PayBillRequest
import com.cluster.core.data.model.PendingTransaction
import com.cluster.core.data.model.ValidateCustomerInfoRequest
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.*
import com.cluster.core.util.delegates.defaultJson
import com.cluster.databinding.BillPaymentFragmentBinding
import com.cluster.executeTransaction
import com.cluster.receipt.billsPaymentReceipt
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant

class BillPaymentFragment : CreditClubFragment(R.layout.bill_payment_fragment) {

    private val viewModel: BillPaymentViewModel by viewModels()
    private val binding: BillPaymentFragmentBinding by dataBinding()
    override val functionId = FunctionIds.PAY_BILL
    private val uniqueReference by lazy { localStorage.newTransactionReference() }
    private val billsPaymentService: BillsPaymentService by retrofitService()
    private val clusterObjectBox: ClusterObjectBox by inject()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAirtime = arguments?.getBoolean("isAirtime") ?: false
        binding.viewModel = viewModel
        binding.toolbar.title = if (isAirtime) "Airtime Purchase" else "Bill Payment"

        binding.completePaymentButton.setOnClickListener {
            mainScope.launch {
                if (viewModel.shouldValidate.value == true) {
                    validateCustomerInformation()
                } else {
                    completePayment()
                }
            }
        }

        viewModel.run {
            categoryList.bindDropDown(category, binding.categoryInput) { this }
            billerList.bindDropDown(biller, binding.billerInput) {
                val categoryId = viewModel.category.value?.id
                filter { b ->
                    b.categoryId == categoryId || "${b.billerCategoryId}" == categoryId
                }
            }
            itemList.bindDropDown(item, binding.paymentItemInput) {
                filter { b -> "${b.billerId}" == viewModel.biller.value?.id }
            }

            category.onChange { newCategory ->
                mainScope.launch {
                    biller.postValue(null)
                    billerName.postValue("")
                    item.postValue(null)
                    itemName.postValue("")
                    if (newCategory != null) {
                        loadBillers()
                    }
                }
            }

            biller.onChange {
                item.postValue(null)
                itemName.postValue("")
                customerValidationResponse.postValue(null)
            }

            item.onChange { newItem ->
                // Return, if the amount should be inputted manually
                if (newItem?.amount == null || newItem.amount!! <= .0) return@onChange
                amountString.postValue(newItem.amount!!.toString())
            }

            fieldOne.onChange {
                customerValidationResponse.postValue(null)
            }

            customerValidationResponse.onChange {
                itemName.postValue("")
                item.postValue(null)
                itemList.postValue(emptyList())
            }

            customerValidatedOrSkipped.onChange { customerValidatedOrSkipped ->
                item.postValue(null)
                itemName.postValue("")
                // Verify customer has been validated if its required
                if (customerValidatedOrSkipped == true) {
                    // Verify a biller has been selected
                    if (biller.value != null) {
                        mainScope.launch {
                            loadPaymentItems()
                        }
                    }
                }
            }
        }
        if (isAirtime) {
            val airtimeCategory = BillCategory(
                id = getString(R.string.bills_airtime_category_id),
                name = "Airtime Purchase",
                description = "Recharge your phone",
                isAirtime = true,
            )
            viewModel.categoryName.postValue(airtimeCategory.name)
            viewModel.hideCategoryField.postValue(true)
            viewModel.category.postValue(airtimeCategory)
        } else {
            mainScope.launch { loadCategories() }
        }
        binding.fieldOneInput.setEndIconOnClickListener {
            mainScope.launch { validateCustomerInformation() }
        }

        binding.categoryInputLayout.setStartIconOnClickListener {
            mainScope.launch { loadCategories() }
        }
        binding.billerInputLayout.setStartIconOnClickListener {
            mainScope.launch { loadBillers() }
        }
        binding.paymentItemInputLayout.setStartIconOnClickListener {
            mainScope.launch { loadPaymentItems() }
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

    private inline fun <T> LiveData<T>.onChange(crossinline block: (value: T?) -> Unit) {
        var oldValue = value
        observe(viewLifecycleOwner) {
            if (it != oldValue) {
                oldValue = it
                block(it)
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

    private suspend fun loadCategories() =
        loadDependencies("categories") {
            val categoryList = billsPaymentService.getBillerCategories(
                localStorage.institutionCode
            )
            viewModel.categoryList.postValue(categoryList)
            viewModel.category.postValue(null)
        }

    private suspend fun loadBillers() =
        loadDependencies("billers") {
            val billerList = billsPaymentService.getBillers(
                localStorage.institutionCode,
                viewModel.category.value?.id
            )
            viewModel.billerList.postValue(billerList)
            viewModel.biller.postValue(null)
        }

    private suspend fun loadPaymentItems() =
        loadDependencies("payment items") {
            val itemList = billsPaymentService.getPaymentItems(
                institutionCode = localStorage.institutionCode,
                billerId = viewModel.biller.value?.id,
                customerId = viewModel.fieldOne.value,
            )
            viewModel.itemList.postValue(itemList)
            viewModel.item.postValue(null)
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

    private suspend fun validateCustomerInformation() = coroutineScope {
        if (viewModel.fieldOneIsNeeded.value == true) {
            val fieldOne = viewModel.fieldOne.value
            if (fieldOne.isBlank()) {
                dialogProvider.showError("${viewModel.fieldOneLabel.value} should not be empty")
                return@coroutineScope
            }
        }

        dialogProvider.showProgressBar("Validating customer details", isCancellable = true) {
            onClose {
                cancel()
            }
        }
        val (response, error) = safeRunIO {
            billsPaymentService.validateCustomerInfo(
                ValidateCustomerInfoRequest(
                    billerId = viewModel.biller.value!!.id!!,
                    customerId = viewModel.fieldOne.value,
                    institutionCode = localStorage.institutionCode!!,
                )
            )
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
            val message = response.responseMessage ?: getString(R.string.network_error_message)
            dialogProvider.showErrorAndWait(message)
            return@coroutineScope
        }

        viewModel.customerValidationResponse.postValue(response)
    }

    private suspend fun completePayment() {
        val billerItem = viewModel.item.value!!
        val category = viewModel.category.value!!
        val isAirtime = viewModel.category.value?.isAirtime ?: false

        if (viewModel.shouldValidate.value == true) {
            validateCustomerInformation()
            if (viewModel.customerValidationResponse.value == null) {
                return
            }
        }

        if (viewModel.fieldTwoIsNeeded.value == true) {
            val fieldTwo = viewModel.fieldTwo.value
            if (fieldTwo.isBlank()) {
                return dialogProvider.showError("${viewModel.fieldTwoLabel.value} should not be empty")
            }
        }

        if (viewModel.amountString.value.isBlank()) {
            return dialogProvider.showErrorAndWait("Amount is required")
        }

        if (viewModel.amountIsNeeded.value == true) {
            val amountString = viewModel.amountString.value
            if (amountString.isBlank()) {
                return dialogProvider.showError("Please enter an amount")
            }
        }

        var customerName = viewModel.customerName.value
        if (customerName.isBlank()) {
            customerName = viewModel.customerValidationName.value ?: ""
        }
        if (viewModel.category.value?.isAirtime != true) {
            if (customerName.isBlank()) return dialogProvider.showError("Customer Name is required")
            if (customerName.includesSpecialCharacters() || customerName.includesNumbers()) {
                return dialogProvider.showError("Customer Name is invalid")
            }

            val customerPhone = viewModel.customerPhone.value
            if (customerPhone.isBlank()) return dialogProvider.showError("Customer Phone is required")
            if (customerPhone.length != 11) return dialogProvider.showError("Customer Phone must be 11 digits")
        }

        val customerEmailValue = viewModel.customerEmail.value
        if (customerEmailValue.isNotBlank() && !customerEmailValue.isValidEmail()) {
            return dialogProvider.showErrorAndWait("Customer Email is invalid")
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.length != 4) return dialogProvider.showError("Agent PIN must be 4 digits long")

        val agent = localStorage.agent
        val request = PayBillRequest(
            agentPin = pin,
            agentPhoneNumber = localStorage.agentPhone,
            agentCode = agent!!.agentCode,
            institutionCode = localStorage.institutionCode,
            customerId = viewModel.fieldOne.value,
            merchantBillerIdField = viewModel.item.value?.billerId?.toString(),
            billItemID = billerItem.id,
            amount = viewModel.amountString.value,
            billerCategoryID = category.id,
            customerEmail = customerEmailValue,
            accountNumber = localStorage.agentPhone,
            billerName = viewModel.biller.value?.name,
            paymentItemCode = billerItem.paymentCodeField,
            paymentItemName = billerItem.name,
            billerCategoryName = category.name,
            customerName = customerName,

            customerPhone = if (isAirtime) {
                viewModel.fieldOne.value
            } else {
                viewModel.customerPhone.value
            },
            customerDepositSlipNumber = uniqueReference,
            geolocation = localStorage.lastKnownLocation,
            isRecharge = isAirtime,
            retrievalReferenceNumber = uniqueReference,
            deviceNumber = localStorage.deviceNumber,
            validationCode = viewModel.customerValidationResponse.value?.validationCode,
        )
        dialogProvider.showProgressBar("Processing request")
        val requestTime = Instant.now()
        val pendingTransactionsBox: Box<PendingTransaction> = clusterObjectBox.boxStore.boxFor()
        val pendingTransaction = PendingTransaction(
            transactionType = if (isAirtime) TransactionType.Recharge else TransactionType.BillsPayment,
            requestJson = defaultJson.encodeToString(
                PayBillRequest.serializer(),
                request,
            ),
            accountName = billerItem.name!!,
            accountNumber = customerName,
            amount = request.amount.toDouble(),
            reference = request.retrievalReferenceNumber,
            createdAt = requestTime,
            lastCheckedAt = null,
        )
        val (response, error) = executeTransaction(
            fetcher = { billsPaymentService.runTransaction(request) },
            reFetcher = { billsPaymentService.billPaymentStatus(request) },
            pendingTransaction = pendingTransaction,
            pendingTransactionsBox = pendingTransactionsBox,
            dialogProvider = dialogProvider,
        )
        dialogProvider.hideProgressBar()
        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return
        }

        val receipt = billsPaymentReceipt(
            context = requireContext(),
            request = request,
            response = response,
            transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm"),
        )
        navigateToReceipt(receipt, popBackStack = true)
    }

    private fun <T> MutableStateFlow<T>.postValue(value: T) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            emit(value)
        }
    }
}
