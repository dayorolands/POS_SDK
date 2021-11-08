package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.*
import com.creditclub.core.data.model.*
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.toCurrencyFormat
import kotlinx.coroutines.flow.*

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
    val requiresValidation = biller.map {
        !it?.customerField1.isNullOrBlank() && biller.value?.isAirtime == false
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
        category?.isAirtime != true
                && !biller?.customerField1.isNullOrBlank()
                && customerValidationResponse == null
    }
    val shouldValidate: LiveData<Boolean> = shouldValidateFlow.asLiveData()
    val primaryButtonText: LiveData<String> = shouldValidateFlow.map {
        if (it) "Validate customer" else "Pay"
    }.asLiveData()


    // Renewal
    private val renewalInfoFlow: Flow<RenewalInfo?> = customerValidationResponse.map {
        if (it?.additionalInformation == null) return@map null
        return@map defaultJson.decodeFromString(
            ValidateCustomerInfoResponse.Additional.serializer(),
            it.additionalInformation!!,
        ).renewalInfo
    }
    val isRenewable: LiveData<Boolean> = renewalInfoFlow.map { it != null }.asLiveData()
    val renewalInfo: LiveData<RenewalInfo?> = renewalInfoFlow.asLiveData()
    val renewalButtonText: LiveData<String> = renewalInfoFlow.map {
        "Renew (${it?.renewalAmount?.toCurrencyFormat() ?: "NGN0.00"})"
    }.asLiveData()
}