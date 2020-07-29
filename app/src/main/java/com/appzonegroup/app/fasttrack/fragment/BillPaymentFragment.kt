package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.BillPaymentFragmentBinding
import com.appzonegroup.app.fasttrack.receipt.BillsPaymentReceipt
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import com.creditclub.pos.printer.PosPrinter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*

class BillPaymentFragment : CreditClubFragment(R.layout.bill_payment_fragment) {

    private val viewModel by viewModels<BillPaymentViewModel>()
    private val binding by dataBinding<BillPaymentFragmentBinding>()
    override val functionId = FunctionIds.PAY_BILL
    private val request = PayBillRequest()
    private val uniqueReference = UUID.randomUUID().toString()
    private val posPrinter: PosPrinter by inject { parametersOf(requireContext(), dialogProvider) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.toolbar.title = "Bill Payment"

        binding.completePaymentButton.setOnClickListener {
            mainScope.launch { completePayment() }
        }

        viewModel.run {
            categoryList.bindDropDown(category, binding.categoryInput) {
                map { it.name ?: "Unknown" }
            }
            billerList.bindDropDown(biller, binding.billerInput) {
                val categoryId = viewModel.category.value?.id
                filter { b ->
                    b.categoryId == categoryId || "${b.billerCategoryId}" == categoryId
                }.map { it.name ?: "Unknown" }
            }
            itemList.bindDropDown(item, binding.paymentItemInput) {
                filter { b -> "${b.billerId}" == viewModel.biller.value?.id }
                    .map { it.name ?: "Unknown" }
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
        mainScope.launch { loadCategories() }
    }

    private inline fun <T> MutableLiveData<T>.onChange(crossinline block: (value: T?) -> Unit) {
        var oldValue = value
        observe(viewLifecycleOwner, Observer {
            if (it != oldValue) {
                oldValue = it
                block(it)
            }
        })
    }

    private inline fun <T> MutableLiveData<List<T>>.bindDropDown(
        selectedItemLiveData: MutableLiveData<T>,
        autoCompleteTextView: AutoCompleteTextView,
        crossinline mapFunction: List<T>.() -> List<String>
    ) {
        observe(viewLifecycleOwner, Observer { list ->
            val items = list?.mapFunction() ?: emptyList()
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
            autoCompleteTextView.setAdapter(adapter)
            if (list != null) {
                autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                    selectedItemLiveData.postValue(list[position])
                }
            }
        })
    }

    private suspend fun loadCategories() =
        loadDependencies("category list") {
            val categoryList = creditClubMiddleWareAPI.billsPaymentService.getBillerCategories(
                localStorage.institutionCode
            )
            viewModel.categoryList.postValue(categoryList)
        }

    private suspend fun loadBillers() =
        loadDependencies("biller list") {
            val billerList = creditClubMiddleWareAPI.billsPaymentService.getBillers(
                localStorage.institutionCode,
                viewModel.category.value?.id
            )
            viewModel.billerList.postValue(billerList)
        }

    private suspend fun loadItems() =
        loadDependencies("item list") {
            val itemList = creditClubMiddleWareAPI.billsPaymentService.getPaymentItems(
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

    private suspend fun completePayment() {
        val isAirtime = viewModel.category.value?.isAirtime ?: false
        val billerItem = viewModel.item.value
        val category = viewModel.category.value
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


        if (viewModel.amountString.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Amount is required")
        }

        if (viewModel.amountIsNeeded.value == true) {
            val amountString = viewModel.amountString.value
            if (amountString.isNullOrBlank()) {
                return dialogProvider.showError("Please enter an amount")
            } else {
                billerItem.amount = amountString.toDoubleOrNull()
            }
        }

        if (viewModel.fieldOneIsNeeded.value == true) {
            val fieldOne = viewModel.fieldOne.value
            if (fieldOne.isNullOrBlank()) {
                return dialogProvider.showError("${viewModel.fieldOneLabel.value} should not be empty")
            } else {
                billerItem.customerFieldOneField = fieldOne
            }
        }

        if (viewModel.fieldTwoIsNeeded.value == true) {
            val fieldTwo = viewModel.fieldTwo.value
            if (fieldTwo.isNullOrBlank()) {
                return dialogProvider.showError("${viewModel.fieldTwoLabel.value} should not be empty")
            } else {
                billerItem.customerFieldOneField = fieldTwo
            }
        }

        val customerEmailValue = viewModel.customerEmail.value
        if (!customerEmailValue.isNullOrBlank() && !customerEmailValue.isValidEmail()) {
            return dialogProvider.showErrorAndWait("Customer Email is invalid")
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return

        request.apply {
            agentPin = pin
            agentPhoneNumber = localStorage.agentPhone
            institutionCode = localStorage.institutionCode
            customerId = viewModel.fieldOne.value
            merchantBillerIdField = viewModel.item.value?.billerId?.toString()
            billItemID = billerItem.id
            amount = viewModel.amountString.value
            billerCategoryID = category.id
            customerEmail = customerEmailValue
            accountNumber = localStorage.agentPhone
            billerName = viewModel.biller.value?.name
            paymentItemCode = billerItem.paymentCodeField
            paymentItemName = billerItem.name
            billerCategoryName = category.name
            customerName = viewModel.customerName.value

            customerPhone = if (isAirtime) {
                viewModel.fieldOne.value
            } else viewModel.customerPhone.value

            customerDepositSlipNumber = uniqueReference
            geolocation = gps.geolocationString
            isRecharge = isAirtime
        }
        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.billsPaymentService.runTransaction(request)
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            dialogProvider.showErrorAndWait("An error occurred. Please try again later")
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

        if (Platform.hasPrinter) {
            val receipt = BillsPaymentReceipt(requireContext(), request).withResponse(response)
            posPrinter.print(receipt)
        }

        activity?.onBackPressed()
    }
}