package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.model.CustomerAccount
import com.creditclub.core.data.response.NameEnquiryResponse

class FundsTransferViewModel : ViewModel() {
    val isSameBank = MutableLiveData(false)
    val receiverAccountNumber = MutableLiveData<String>()
    val amountString = MutableLiveData<String>()
    val nameEnquiryResponse: MutableLiveData<NameEnquiryResponse> = MutableLiveData()
    val accountName = Transformations.map(nameEnquiryResponse) { it?.beneficiaryAccountName }
    val narration = MutableLiveData<String>()
    val bank: MutableLiveData<Bank> = MutableLiveData()
    val bankList: MutableLiveData<List<Bank>> = MutableLiveData()
}