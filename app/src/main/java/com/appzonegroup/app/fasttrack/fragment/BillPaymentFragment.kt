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
import com.appzonegroup.app.fasttrack.receipt.BillsPaymentReceipt
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.api.BillsPaymentService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.data.model.ValidateCustomerInfoRequest
import com.creditclub.core.data.prefs.newTransactionReference
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.data.response.PayBillResponse
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.includesNumbers
import com.creditclub.core.util.includesSpecialCharacters
import com.creditclub.core.util.isValidEmail
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.printer.PosPrinter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class BillPaymentFragment : CreditClubFragment(R.layout.bill_payment_fragment) {

    private val viewModel by viewModels<BillPaymentViewModel>()
    private val binding by dataBinding<BillPaymentFragmentBinding>()
    override val functionId = FunctionIds.PAY_BILL
    private val uniqueReference by lazy { localStorage.newTransactionReference() }
    private val posPrinter: PosPrinter by inject { parametersOf(requireContext(), dialogProvider) }
    private val billsPaymentService by retrofitService<BillsPaymentService>()

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
        crossinline mapFunction: List<T>.() -> List<Any>
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
        crossinline fetcher: suspend () -> Unit
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
            amount = viewModel.amountString.value,
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
            } else viewModel.customerPhone.value,
            customerDepositSlipNumber = uniqueReference,
            geolocation = localStorage.lastKnownLocation,
            isRecharge = isAirtime,
            retrievalReferenceNumber = uniqueReference,
            validationCode = viewModel.customerValidationResponse.value?.validationCode,
        )
        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            billsPaymentService.runTransaction(request)
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            dialogProvider.showErrorAndWait("An error occurred. Please try again later")
            printReceipt(request, null)
            activity?.onBackPressed()
            return
        }

        if (response.isSuccessFul == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Transaction successful")
        } else {
            val message = response.responseMessage
                ?: getString(R.string.an_error_occurred_please_try_again_later)
            dialogProvider.showErrorAndWait(message)
        }

        printReceipt(request, response)
        activity?.onBackPressed()
    }

    private suspend fun printReceipt(request: PayBillRequest, response: PayBillResponse?) {
        if (Platform.hasPrinter) {
            val receipt = BillsPaymentReceipt(requireContext(), request).withResponse(response)
            posPrinter.print(receipt)
        }
    }
}