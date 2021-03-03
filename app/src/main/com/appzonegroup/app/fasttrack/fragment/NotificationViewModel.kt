package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
}