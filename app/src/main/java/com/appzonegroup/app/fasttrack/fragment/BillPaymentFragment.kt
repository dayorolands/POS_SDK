package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.BillPaymentFragmentBinding
import com.appzonegroup.app.fasttrack.executeTransaction
import com.appzonegroup.app.fasttrack.receipt.billsPaymentReceipt
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.api.BillsPaymentService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.model.ValidateCustomerInfoRequest
import com.creditclub.core.data.prefs.newTransactionReference
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.type.TransactionType
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.defaultJson
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
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
            mainScope.launch { completePayment() }
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
                    biller.value = null
                    billerName.value = null
                    item.value = null
                    itemName.value = null
                    if (newCategory != null) {
                        loadBillers()
                    }
                }
            }

            biller.onChange { newBiller ->
                mainScope.launch {
                    item.value = null
                    itemName.value = null
                    customerValidationResponse.value = null
                    if (newBiller != null) {
                        loadItems()
                    }
                }
            }

            item.onChange { newItem ->
                mainScope.launch {
                    if (amountIsNeeded.value == false) {
                        amountString.value = newItem?.amount?.toString()
                    }
                }
            }
        }
        if (isAirtime) {
            viewModel.categoryName.value = "Airtime Purchase"
            viewModel.hideCategoryField.value = true
            viewModel.category.value = BillCategory(
                id = getString(R.string.bills_airtime_category_id),
                name = "Airtime Purchase",
                description = "Recharge your phone",
                isAirtime = true,
            )
        } else {
            mainScope.launch { loadCategories() }
        }
        binding.fieldOneInput.setEndIconOnClickListener {
            mainScope.launch { validateCustomerInformation() }
        }
    }

    private inline fun <T> MutableLiveData<T>.onChange(crossinline block: (value: T?) -> Unit) {
        var oldValue = value
        observe(viewLifecycleOwner) {
            if (it != oldValue) {
                oldValue = it
                block(it)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> MutableLiveData<List<T>>.bindDropDown(
        selectedItemLiveData: MutableLiveData<T>,
        autoCompleteTextView: AutoCompleteTextView,
        crossinline mapFunction: List<T>.() -> List<Any>,
    ) {
        observe(viewLifecycleOwner) { list ->
            val items = list?.mapFunction() ?: emptyList()
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
            autoCompleteTextView.setAdapter(adapter)
            if (list != null) {
                autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                    selectedItemLiveData.postValue(parent.getItemAtPosition(position) as T)
                }
            }
        }
    }

    private suspend fun loadCategories() =
        loadDependencies("category list") {
            val categoryList = billsPaymentService.getBillerCategories(
                localStorage.institutionCode
            )
            viewModel.categoryList.postValue(categoryList)
        }

    private suspend fun loadBillers() =
        loadDependencies("biller list") {
            val billerList = billsPaymentService.getBillers(
                localStorage.institutionCode,
                viewModel.category.value?.id
            )
            viewModel.billerList.postValue(billerList)
        }

    private suspend fun loadItems() =
        loadDependencies("item list") {
            val itemList = billsPaymentService.getPaymentItems(
                localStorage.institutionCode,
                viewModel.biller.value?.id
            )
            viewModel.itemList.postValue(itemList)
        }

    private suspend inline fun loadDependencies(
        dependencyName: String,
        crossinline fetcher: suspend () -> Unit,
    ) {
        dialogProvider.showProgressBar("Loading $dependencyName")
        val (items) = safeRunIO { fetcher() }
        dialogProvider.hideProgressBar()

        if (items == null) {
            dialogProvider.showErrorAndWait("An error occurred while loading $dependencyName")
            return
        }
    }

    private suspend fun validateCustomerInformation() {
        val billerItem = viewModel.item.value
        val biller = viewModel.biller.value

        if (viewModel.fieldOneIsNeeded.value == true) {
            val fieldOne = viewModel.fieldOne.value
            if (fieldOne.isNullOrBlank()) {
                return dialogProvider.showError("${viewModel.fieldOneLabel.value} should not be empty")
            } else {
                billerItem!!.customerFieldOneField = fieldOne
            }
        }

        if (viewModel.fieldTwoIsNeeded.value == true) {
            val fieldTwo = viewModel.fieldTwo.value
            if (fieldTwo.isNullOrBlank()) {
                return dialogProvider.showError("${viewModel.fieldTwoLabel.value} should not be empty")
            } else {
                billerItem!!.customerFieldOneField = fieldTwo
            }
        }

        if (viewModel.amountString.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Amount is required")
        }

        if (viewModel.amountIsNeeded.value == true) {
            val amountString = viewModel.amountString.value
            if (amountString.isNullOrBlank()) {
                return dialogProvider.showError("Please enter an amount")
            } else {
                billerItem!!.amount = amountString.toDoubleOrNull()
            }
        }

        dialogProvider.showProgressBar("Validating customer details")
        val (response, error) = safeRunIO {
            billsPaymentService.validateCustomerInfo(
                ValidateCustomerInfoRequest(
                    amount = billerItem!!.amount!!,
                    billerId = biller!!.id!!,
                    customerId = viewModel.fieldOne.value!!,
                    institutionCode = localStorage.institutionCode!!,
                )
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            dialogProvider.showErrorAndWait(getString(R.string.an_error_occurred_please_try_again_later))
            return
        }

        if (!response.isSuccessful) {
            val message = response.responseMessage ?: getString(R.string.network_error_message)
            dialogProvider.showErrorAndWait(message)
            return
        }

        viewModel.customerValidationResponse.value = response
    }


    private suspend fun completePayment() {
        val billerItem = viewModel.item.value
        val category = viewModel.category.value
        val isAirtime = viewModel.category.value?.isAirtime ?: false
        val biller = viewModel.biller.value

        if (category == null) {
            return dialogProvider.showErrorAndWait("Please select a category")
        }

        if (biller == null) {
            return dialogProvider.showError("Please select a biller")
        }

        if (billerItem == null) {
            return dialogProvider.showError("Please select an item")
        }

        if (viewModel.category.value?.isAirtime != true &&
            viewModel.customerValidationResponse.value == null &&
            viewModel.fieldOneIsNeeded.value == true
        ) {
            validateCustomerInformation()
            if (viewModel.customerValidationResponse.value == null) {
                return
            }
        }

        if (viewModel.category.value?.isAirtime != true) {
            val customerName = viewModel.customerName.value
            if (customerName.isNullOrBlank()) return dialogProvider.showError("Customer Name is required")
            if (customerName.includesSpecialCharacters() || customerName.includesNumbers()) {
                return dialogProvider.showError("Customer Name is invalid")
            }

            val customerPhone = viewModel.customerPhone.value
            if (customerPhone.isNullOrBlank()) return dialogProvider.showError("Customer Phone is required")
            if (customerPhone.length != 11) return dialogProvider.showError("Customer Phone must be 11 digits")
        }

        val customerEmailValue = viewModel.customerEmail.value
        if (!customerEmailValue.isNullOrBlank() && !customerEmailValue.isValidEmail()) {
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
            amount = viewModel.amountString.value!!,
            billerCategoryID = category.id,
            customerEmail = customerEmailValue,
            accountNumber = localStorage.agentPhone,
            billerName = viewModel.biller.value?.name,
            paymentItemCode = billerItem.paymentCodeField,
            paymentItemName = billerItem.name,
            billerCategoryName = category.name,
            customerName = viewModel.customerName.value,

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
            accountNumber = viewModel.customerName.value ?: "",
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
}