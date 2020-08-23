package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.service.ApiService
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.pos.PosConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers

suspend fun logPosNotification(
    db: PosDatabase,
    backendConfig: BackendConfig,
    posConfig: PosConfig,
    posNotification: PosNotification
) = withContext(Dispatchers.IO) {
    db.posNotificationDao().save(posNotification)

    val url =
        "${backendConfig.apiHost}/CreditClubMiddlewareAPI/CreditClubStatic/POSCashOutNotification"

    val dataToSend = Gson().toJson(posNotification)

    val headers = Headers.Builder()
    headers.add(
        "Authorization",
        "iRestrict ${backendConfig.posNotificationToken}"
    )
    headers.add("TerminalID", posConfig.terminalId)

    val (responseString, error) = ApiService.post(
        url,
        dataToSend,
        headers.build()
    )

    responseString ?: return@withContext
    error?.printStackTrace()

    try {
        val notificationResponse =
            Gson().fromJson(
                responseString,
                NotificationResponse::class.java
            )
        if (notificationResponse != null) {
            if (notificationResponse.billerReference != null && notificationResponse.billerReference!!.isNotEmpty()) {
                db.posNotificationDao().delete(posNotification.id)
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}