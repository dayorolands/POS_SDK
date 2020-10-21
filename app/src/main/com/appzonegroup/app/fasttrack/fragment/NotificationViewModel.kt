package com.appzonegroup.app.fasttrack.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creditclub.core.data.model.Notification

class NotificationViewModel : ViewModel() {
    val notificationList = MutableLiveData<List<Notification>>()
}