package com.appzonegroup.app.fasttrack.fragment

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.data.model.CollectionCategory
import com.creditclub.core.data.model.CollectionCustomer
import com.creditclub.core.data.model.CollectionPaymentItem
import com.creditclub.core.data.model.CollectionReference
import com.creditclub.core.util.toCurrencyFormat
import com.google.android.material.textfield.TextInputLayout

class CollectionPaymentViewModel : ViewModel() {
    val collectionReference: MutableLiveData<CollectionReference> = MutableLiveData()
    val region: MutableLiveData<String> = MutableLiveData()
    val category: MutableLiveData<CollectionCategory> = MutableLiveData()
    val categoryCode = Transformations.map(category) { it?.code }
    val categoryName: MutableLiveData<String> = MutableLiveData()

    val item: MutableLiveData<CollectionPaymentItem> = MutableLiveData()
    val itemName: MutableLiveData<String> = MutableLiveData()
    val itemCode = Transformations.map(item) { it?.code }

    val collectionType: MutableLiveData<String> = MutableLiveData()
    val collectionService: MutableLiveData<String> = MutableLiveData()
    val customerId: MutableLiveData<String> = MutableLiveData()
    val referenceString: MutableLiveData<String> = MutableLiveData()
    val customer: MutableLiveData<CollectionCustomer> = MutableLiveData()

    val referenceName = Transformations.map(collectionReference) { it?.referenceName }
    val customerPhoneNumber: MutableLiveData<String> = MutableLiveData()
    val customerName = Transformations.map(customer) { it?.name }
    val amountString = Transformations.map(collectionReference) {
        it?.amount?.toCurrencyFormat(it.currency ?: "NGN")
    }
    val validReference = Transformations.map(collectionReference) { it != null }
    val collectionTypeIsWebGuid = Transformations.map(collectionType) { it == "WEBGUID" }

    companion object {
        @JvmStatic
        @BindingAdapter("app:goneUnless")
        fun goneUnless(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }

        @JvmStatic
        @BindingAdapter("app:dependsOn")
        fun dependsOn(view: View, data: LiveData<String>) {
            view.isEnabled = !data.value.isNullOrBlank()
        }

        @JvmStatic
        @BindingAdapter("app:disabledIfPresent")
        fun disabledIfPresent(view: View, data: LiveData<*>) {
            view.isEnabled = when (data.value) {
                is String? -> !(data.value as? String).isNullOrBlank()
                else -> data.value != null
            }
        }

        @JvmStatic
        @BindingAdapter("app:showEndIconIfPresent")
        fun showEndIconIfPresent(textInputLayout: TextInputLayout, data: LiveData<*>) {
            textInputLayout.isEndIconVisible = when (data.value) {
                is String? -> !(data.value as? String).isNullOrBlank()
                else -> data.value != null
            }
        }
    }
}