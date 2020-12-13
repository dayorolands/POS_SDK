package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.AccountInfo

class WithdrawalViewModel : ViewModel() {
    val accountNumber = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val amountString = MutableLiveData<String>()
    val tokenSent = MutableLiveData<Boolean>()
    val hasExternalToken = MutableLiveData<Boolean>()
    val accountInfo: MutableLiveData<AccountInfo> = MutableLiveData()
    val accountName = Transformations.map(accountInfo) { it?.accountName }
    val showPhoneNumberInput = MutableLiveData<Boolean>()
//    Transformations.map(accountInfo) {
//        hasExternalToken.value == true && it?.phoneNumber == null
//    }
}