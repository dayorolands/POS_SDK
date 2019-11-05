package com.appzonegroup.creditclub.pos.models.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosNotification
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationDao = PosDatabase.getInstance(
        application
    ).posNotificationDao()

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    internal val unsettledTransactions: LiveData<List<PosNotification>>
        get() = notificationDao.allAsync()

    internal fun saveNotification(posNotification: PosNotification) {
        executorService.execute { notificationDao.save(posNotification) }
    }

    internal fun deleteNotification(posNotification: PosNotification) {
        executorService.execute { notificationDao.delete(posNotification) }
    }
}