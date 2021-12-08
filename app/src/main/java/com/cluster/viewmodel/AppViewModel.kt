package com.cluster.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cluster.core.data.model.CaseDetail
import com.cluster.core.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow

class AppViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
    val caseDetails = mutableStateOf<List<CaseDetail>>(emptyList())
    val fcmToken = mutableStateOf("")
    val sessionTimedOut = MutableLiveData(false)
}