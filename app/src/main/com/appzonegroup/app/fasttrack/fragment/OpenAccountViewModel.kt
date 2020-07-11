package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OpenAccountViewModel : ViewModel() {
    val passportString = MutableLiveData<String>()
    var bvn = MutableLiveData<String>()
    var dob = MutableLiveData<String>()
    var agentPIN = MutableLiveData<String>()

    var surname = MutableLiveData<String>()
    var email = MutableLiveData<String>()
    var firstName = MutableLiveData<String>()
    var middleName = MutableLiveData<String>()
    var phoneNumber = MutableLiveData<String>()
    var gender = MutableLiveData<String>()
    var address = MutableLiveData<String>()
    var state = MutableLiveData<String>()
    var placeOfBirth = MutableLiveData<String>()
    var starterPackNo = MutableLiveData<String>()
    var productName = MutableLiveData<String>()
    var productCode = MutableLiveData<String>()

    val afterAccountInfo = MutableLiveData<Function0<Unit>>()
    val afterGeneralInfo = MutableLiveData<Function0<Unit>>()
    val afterDocumentUpload = MutableLiveData<Function0<Unit>>()
    val afterAgentPin = MutableLiveData<Function0<Unit>>()
}