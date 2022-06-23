package com.cluster.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.cluster.core.data.model.*

class CollectionPaymentViewModel : ViewModel() {
    val retrievalReferenceNumber: MutableLiveData<String> = MutableLiveData()

    val billerList: MutableLiveData<List<CollectionCategory>> = MutableLiveData()
    val billers: MutableLiveData<CollectionCategory> = MutableLiveData()
    val billerName: MutableLiveData<String> = MutableLiveData()
    val billerId = Transformations.map(billers) {it.id}

    val paymentItemList: MutableLiveData<List<CollectionPaymentItem>> = MutableLiveData()
    val paymentItem: MutableLiveData<CollectionPaymentItem> = MutableLiveData()
    val paymentItemName: MutableLiveData<String> = MutableLiveData()
    val paymentItemAmount = MutableLiveData<String>()

    val validCustomerName = MutableLiveData<String>()
    val validCustomerEmail = MutableLiveData<String>()
    val customerPhoneNumber= MutableLiveData<String>()
    val customerValue = MutableLiveData<String>()

    val customerValidResp : MutableLiveData<CustomerValidationResponse> = MutableLiveData()
    val paymentInformation: MutableLiveData<CustomerValidationResponse.PaymentInformation> = MutableLiveData()

    val collectionService: MutableLiveData<String> = MutableLiveData()

}