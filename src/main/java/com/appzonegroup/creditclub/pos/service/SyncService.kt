package com.appzonegroup.creditclub.pos.service

import android.content.Intent
import android.util.Log
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.contract.Logger
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.appzonegroup.creditclub.pos.util.MyReceiver
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncService : BaseService(), Logger {
    override val tag: String = "SyncService"

    private val intent by lazy { Intent(this, MyReceiver::class.java) }
//    private val am by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }
//    private val pi: PendingIntent by lazy { PendingIntent.getBroadcast(this, 1, intent, 0) }

//    private var interval = 60000 * 10 // 60  minutes

    override fun onCreate() {
        super.onCreate()

//        am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + interval, interval.toLong(), pi)

        log("Starting service")
//        logPosNotifications()
        performReversals()
    }

    //    @Synchronized
    fun logPosNotifications() {
        GlobalScope.launch(Dispatchers.Default) {
            val url = ApiService.BASE_URL + "/POSCashOutNotification"
            val serializer = Gson()
            val dao = PosDatabase.getInstance(this@SyncService).posNotificationDao()
            var notifications = dao.all()
            Log.e("PosNotification", "Starting")

            while (notifications.isNotEmpty()) {
                val dataToSend = serializer.toJson(notifications.first())

                Log.e("PosNotification", dataToSend)
                val (responseString, error) = ApiService.post(url, dataToSend)

                error?.printStackTrace()

                responseString?.also {
                    Log.e("PosNotificationS", responseString)
                    try {
                        val response = serializer.fromJson(responseString, NotificationResponse::class.java)
                        if (response != null) {
                            if (response.isSuccessFul) {
                                dao.delete(notifications.first().id)
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                notifications = dao.all()

                delay(10000)
            }
        }
    }

    fun performReversals() {
        GlobalScope.launch(Dispatchers.Default) {
            log("Starting reversal checker")

            val dao = PosDatabase.getInstance(this@SyncService).reversalDao()
            val localDate = TransmissionDateParams().localDate

            dao.deleteOthers(localDate)

            while (true) {
                val reversals = dao.byDate(localDate)

                for (reversal in reversals) {
                    val request = reversal.isoMsg
                    val success = isoSocketHelper.attempt(request, 4, onReattempt = {
                        request.mti = "421"
                    })

                    if (success) {
//                        Answers.getInstance().logCustom(CustomEvent("Reversal").apply {
//                            putCustomAttribute("TID", config.terminalId)
//                            putCustomAttribute("RRN", request.retrievalReferenceNumber37)
//                        })

                        dao.delete(reversal)
                    }
                }

                delay(10000)
            }
        }
    }

}
