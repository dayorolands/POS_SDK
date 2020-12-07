package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.CaseCategory

class CaseLogViewModel : ViewModel() {
    val subject = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val agentEmail = MutableLiveData<String>()
    val category: MutableLiveData<CaseCategory> = MutableLiveData()
    val categoryList: MutableLiveData<List<CaseCategory>> = MutableLiveData()
    val product: MutableLiveData<String> = MutableLiveData()
    val productList: MutableLiveData<List<String>> = MutableLiveData()
}