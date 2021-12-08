package com.cluster.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cluster.core.data.model.BillCategory
import com.cluster.core.data.model.BillPaymentItem
import com.cluster.core.data.model.Biller
import com.cluster.core.data.model.ValidateCustomerInfoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BillPaymentViewModel : ViewModel() {
    val category = MutableStateFlow<BillCategory?>(null)
    val categoryName = MutableStateFlow("")

    val biller = MutableStateFlow<Biller?>(null)
    val billerName = MutableStateFlow("")

    val item = MutableStateFlow<BillPaymentItem?>(null)
    val itemName = MutableStateFlow("")

    val categoryList = MutableStateFlow(emptyList<BillCategory>())
    val billerList = MutableStateFlow(emptyList<Biller>())
    val itemList = MutableStateFlow(emptyList<BillPaymentItem>())

    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val amountString = MutableStateFlow("")
    val amountIsNeeded = item.map { .0 >= (it?.amount ?: .0) }.asLiveData()

    val fieldOneLabel = biller.map { it?.customerField1 }.asLiveData()
    val fieldOneIsNeeded = biller.map { !it?.customerField1.isNullOrBlank() }.asLiveData()
    val requiresValidation: LiveData<Boolean> = combine(category, biller) { category, biller ->
        if (category == null) return@combine false
        if (biller == null) return@combine false
        !category.isAirtime && !biller.customerField1.isNullOrBlank()
    }.asLiveData()
    val fieldOne = MutableStateFlow("")

    val fieldTwoLabel = biller.map { it?.customerField2 }.asLiveData()
    val fieldTwoIsNeeded = biller.map { !it?.customerField2.isNullOrBlank() }.asLiveData()
    val fieldTwo = MutableStateFlow("")
    val hideCategoryField = MutableStateFlow(false)

    val customerEmail = MutableStateFlow("")

    // Validation
    val customerValidationResponse = MutableStateFlow<ValidateCustomerInfoResponse?>(null)
    val customerValidationName = customerValidationResponse.map {
        it?.customerName
    }.asLiveData()
    val customerValidatedOrSkipped: LiveData<Boolean> = combine(
        customerValidationResponse,
        category,
        biller,
    ) { customerValidationResponse, category, biller ->
        if (category == null) return@combine false
        if (biller == null) return@combine false
        customerValidationResponse != null
                || biller.customerField1.isNullOrBlank()
                || category.isAirtime
    }.asLiveData()

    val requestIsValid: LiveData<Boolean> =
        combine(category, biller, item) { category, biller, item ->
            category != null && biller != null && item != null
        }.asLiveData()

    val isAirtime = category.map { it?.isAirtime == true }.asLiveData()

    private val shouldValidateFlow = combine(
        category,
        biller,
        customerValidationResponse,
    ) { category, biller, customerValidationResponse ->
        if (category == null) return@combine false
        if (biller == null) return@combine false
        !category.isAirtime
                && !biller.customerField1.isNullOrBlank()
                && customerValidationResponse == null
    }
    val shouldValidate: LiveData<Boolean> = shouldValidateFlow.asLiveData()
    val primaryButtonText: LiveData<String> = shouldValidateFlow.map {
        if (it) "Validate customer" else "Pay"
    }.asLiveData()
}