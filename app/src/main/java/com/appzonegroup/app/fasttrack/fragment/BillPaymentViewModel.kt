package com.appzonegroup.app.fasttrack.fragment

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.data.model.BillPaymentItem
import com.creditclub.core.data.model.Biller

class BillPaymentViewModel : ViewModel() {
    val category: MutableLiveData<BillCategory> = MutableLiveData()
    val categoryName = MutableLiveData<String>()

    val biller: MutableLiveData<Biller> = MutableLiveData()
    val billerName = MutableLiveData<String>()

    val item: MutableLiveData<BillPaymentItem> = MutableLiveData()
    val itemName = MutableLiveData<String>()

    val categoryList: MutableLiveData<List<BillCategory>> = MutableLiveData()
    val billerList: MutableLiveData<List<Biller>> = MutableLiveData()
    val itemList: MutableLiveData<List<BillPaymentItem>> = MutableLiveData()

    val customerName = MutableLiveData<String>()
    val customerPhone = MutableLiveData<String>()
    val amountString = MutableLiveData<String>()
    val amountIsNeeded = Transformations.map(item) { .0 >= it?.amount ?: .0 }

    val fieldOneLabel = Transformations.map(biller) { it?.customerField1 }
    val fieldOneIsNeeded = Transformations.map(biller) { !it?.customerField1.isNullOrBlank() }
    val fieldOne = MutableLiveData<String>()

    val fieldTwoLabel = Transformations.map(biller) { it?.customerField2 }
    val fieldTwoIsNeeded = Transformations.map(biller) { !it?.customerField2.isNullOrBlank() }
    val fieldTwo = MutableLiveData<String>()

    val customerEmail = MutableLiveData<String>()

    val isAirtime = Transformations.map(category) { it?.isAirtime == true }
}