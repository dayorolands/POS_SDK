package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OpenAccountViewModel : ViewModel() {
    val passportString = MutableLiveData<String>()
    val bvn = MutableLiveData<String>()
    val dob = MutableLiveData<String>()
    val agentPIN = MutableLiveData<String>()

    val surname = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val firstName = MutableLiveData<String>()
    val middleName = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val stateCode = MutableLiveData<String>()
    val placeOfBirth = MutableLiveData<String>()
    val starterPackNo = MutableLiveData<String>()
    val productName = MutableLiveData<String>()
    val productCode = MutableLiveData<String>()

    val afterAccountInfo = MutableLiveData<Function0<Unit>>()
    val afterGeneralInfo = MutableLiveData<Function0<Unit>>()
    val afterDocumentUpload = MutableLiveData<Function0<Unit>>()
    val afterAgentPin = MutableLiveData<Function0<Unit>>()

    val isWalletAccount = MutableLiveData<Boolean>()
    val requiresState = MutableLiveData<Boolean>()
    val requiresEmail = MutableLiveData<Boolean>()
    val requiresProduct = MutableLiveData<Boolean>()
}