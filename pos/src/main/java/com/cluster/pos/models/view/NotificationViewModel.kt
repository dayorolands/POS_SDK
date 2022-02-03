package com.cluster.pos.models.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.models.PosNotification
import com.cluster.core.util.safeRunIO
import kotlinx.coroutines.launch

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