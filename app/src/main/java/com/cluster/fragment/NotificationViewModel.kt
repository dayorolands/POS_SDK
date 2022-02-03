package com.cluster.fragment

import androidx.lifecycle.ViewModel
import com.cluster.core.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
}