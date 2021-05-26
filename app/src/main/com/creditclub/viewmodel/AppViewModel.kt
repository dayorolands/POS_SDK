package com.creditclub.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.CaseDetail
import com.creditclub.core.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow

class AppViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
    val caseDetails = mutableStateOf<List<CaseDetail>>(emptyList())
    val fcmToken = mutableStateOf("")
}