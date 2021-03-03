package com.appzonegroup.creditclub.pos.models.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    val notificationDao = PosDatabase.getInstance(application).posNotificationDao()

    internal fun deleteNotification(posNotification: PosNotification) {
        viewModelScope.launch {
            safeRunIO {
                notificationDao.delete(posNotification)
            }
        }
    }
}