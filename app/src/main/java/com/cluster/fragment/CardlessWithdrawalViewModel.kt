package com.cluster.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cluster.core.data.api.CustomerValidationInfoResponse
import com.cluster.core.data.model.GetBankResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CardlessWithdrawalViewModel : ViewModel() {
    val bankName = MutableStateFlow<GetBankResponse.Data?>(null)
    val bankNameList = MutableStateFlow(emptyList<GetBankResponse.Data>())

    val hideCategoryField = MutableStateFlow(false)
    val bankNames = MutableStateFlow("")
    val accountNumber = MutableStateFlow("")

    val requiresValidation: LiveData<Boolean> = combine(bankName) {
        bankNameList.value.isNotEmpty()
    }.asLiveData()

    var isSameBank = MutableStateFlow(false)

    //Customer Validation
    val customerNameValidationResponse = MutableStateFlow<CustomerValidationInfoResponse?>(null)
    val customerValidationName = customerNameValidationResponse.map {
        it?.data!!.accountName
    }.asLiveData()

    val amountString = MutableStateFlow("")
    val customerName = MutableStateFlow("")
}