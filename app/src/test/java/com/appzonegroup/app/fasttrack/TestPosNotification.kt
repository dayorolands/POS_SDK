package com.appzonegroup.app.fasttrack

import com.appzonegroup.app.fasttrack.model.PosNotification
import com.appzonegroup.app.fasttrack.network.ApiServiceObject
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/09/2019.
 * Appzone Ltd
 */

class TestPosNotification : CreditClubUnitTest() {


    @Test
    fun `pos notifications are logged successfully`() {
        val url = "${ApiServiceObject.BASE_URL}/${ApiServiceObject.STATIC}/POSCashOutNotification"

        val dataToSend = Gson().toJson(PosNotification())

        val headers = Headers.Builder()
        headers.add("TerminalID", "20390008")
        headers.add("Authorization", "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}")

        mainScope {
            val (responseString, error) = withContext(Dispatchers.IO) {
                ApiServiceObject.post(url, dataToSend, headers.build())
            }

            error?.printStackTrace()

            responseString?.also {
                log("PosNotification response: $responseString")
                try {
                    val response = Gson().fromJson(responseString, NotificationResponse::class.java)
                    if (response != null) {
                        assert(response.billerReference != null)
                        println(response.billerReference)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun log(message: String) = println(message)
}