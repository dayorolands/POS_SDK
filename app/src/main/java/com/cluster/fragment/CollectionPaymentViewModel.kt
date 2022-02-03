package com.cluster.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.cluster.core.data.model.CollectionCategory
import com.cluster.core.data.model.CollectionCustomer
import com.cluster.core.data.model.CollectionPaymentItem
import com.cluster.core.data.model.CollectionReference

class CollectionPaymentViewModel : ViewModel() {
    val retrievalReferenceNumber: MutableLiveData<String> = MutableLiveData()
    val collectionReference: MutableLiveData<CollectionReference> = MutableLiveData()
    val collectionReferenceItemCode = Transformations.map(collectionReference) { it?.itemCode }
    val collectionReferenceCategoryCode =
        Transformations.map(collectionReference) { it?.categoryCode }

    val region: MutableLiveData<String> = MutableLiveData()
    val category: MutableLiveData<CollectionCategory> = MutableLiveData()
    val categoryCode = Transformations.map(category) { it?.code }
    val categoryName: MutableLiveData<String> = MutableLiveData()

    val item: MutableLiveData<CollectionPaymentItem> = MutableLiveData()
    val itemName: MutableLiveData<String> = MutableLiveData()
    val itemCode: MutableLiveData<String> = MutableLiveData()

    val collectionType: MutableLiveData<String> = MutableLiveData()
    val collectionService: MutableLiveData<String> = MutableLiveData()
    val customerId: MutableLiveData<String> = MutableLiveData()
    val customerType: MutableLiveData<String> = MutableLiveData()
    val referenceString: MutableLiveData<String> = MutableLiveData()
    val customer: MutableLiveData<CollectionCustomer> = MutableLiveData()

    val referenceName = Transformations.map(collectionReference) { it?.referenceName }
    val paymentReferenceName: MutableLiveData<String> = MutableLiveData()
    val customerPhoneNumber: MutableLiveData<String> = MutableLiveData()
    val customerName = Transformations.map(customer) { it?.name }
    val amountString = MutableLiveData<String>()
    val validReference = Transformations.map(collectionReference) { it != null }
    val collectionTypeIsWebGuid = Transformations.map(collectionType) { it == "WEBGUID" }
    val collectionTypeIsCbs = Transformations.map(collectionType) { it == "CBS" }
    val isOffline: MutableLiveData<Boolean> = MutableLiveData()
    val invoiceNumber: MutableLiveData<String> = MutableLiveData()

    val itemList = MutableLiveData<List<CollectionPaymentItem>>()
    val categoryList = MutableLiveData<List<CollectionCategory>>()
}