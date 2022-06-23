package com.cluster.fragment

import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.cluster.core.data.model.*
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.data.request.CollectionPaymentRequest
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.lang.StringBuilder

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

    val customerValidationSur: MutableLiveData<CustomerValidationResponse> = MutableLiveData()

    val acceptPartPayment = MutableLiveData<Boolean>()
    val minimumAmount = MutableLiveData<String>()
    val maximumAmount = MutableLiveData<String>()
    val paymentReferece = MutableLiveData<String>()

    val customerValidResp : MutableLiveData<CustomerValidationResponse> = MutableLiveData()
    val surchargeConfiguration: MutableLiveData<List<CustomerValidationResponse.SurchargeConfiguration>> = MutableLiveData()

    val customFields: MutableLiveData<List<CollectionPaymentItem.CollectionValidationCustomFields>> = MutableLiveData()

    val collectionService: MutableLiveData<String> = MutableLiveData()

    private fun MutableList<CollectionPaymentItem.CollectionValidationCustomFields>.loopThroughCustomFields(customFields: CollectionPaymentItem.CollectionValidationCustomFields){
        val jsonObject = JSONObject(CollectionPaymentItem.toString())
        val jsonArray = jsonObject.optJSONArray("CustomFields")
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.optString("Id").toInt()
            val displayText = jsonObject.optString("DisplayText").toString()
            val isRequired = jsonObject.optBoolean("IsRequired")
            val customName = jsonObject.optString("Name").toString()
        }
    }
}