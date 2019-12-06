package com.appzonegroup.creditclub.pos.service

import android.os.Looper
import android.util.Log
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.contract.Logger
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.creditclub.core.util.safeRunIO
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody

class SyncService : BaseService(), Logger {
    override val tag: String = "SyncService"

    override fun onCreate() {
        super.onCreate()

        log("Starting service")
//        logPosNotifications()
//        performReversals()
//        logIsoRequests()
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
                        val response =
                            serializer.fromJson(responseString, NotificationResponse::class.java)
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

    @Synchronized
    private fun logIsoRequests() {

        ioScope.launch {
            Looper.myLooper() ?: Looper.prepare()
            val dao = posDatabase.isoRequestLogDao()
            Log.e("IsoRequestLog", "Starting")

            val mediaType = MediaType.parse("application/json")

            while (true) {
                val requestLogs = dao.all()

                for (requestLog in requestLogs) {
                    val requestBody = RequestBody.create(
                        mediaType,
                        Json.stringify(IsoRequestLog.serializer(), requestLog)
                    )

                    val (response) = safeRunIO {
                        creditClubMiddleWareAPI.staticService.logToGrafanaForPOSTransactions(
                            requestBody,
                            "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}",
                            requestLog.terminalId
                        )
                    }

                    if (response?.status == true) {
                        dao.delete(requestLog)
                    }
                }

                delay(10000)
            }
        }
    }

    @Synchronized
    private fun performReversals() {
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
