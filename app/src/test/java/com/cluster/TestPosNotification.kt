package com.cluster

import com.cluster.pos.models.PosNotification
import com.cluster.core.util.delegates.service
import com.cluster.core.util.safeRunIO
import com.cluster.pos.api.PosApiService
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/09/2019.
 * Appzone Ltd
 */

class TestPosNotification : CreditClubUnitTest() {
    @Test
    fun `pos notifications are logged successfully`() {
        val posApiService: PosApiService by creditClubMiddleWareAPI.retrofit.service()
        val notification = PosNotification()

        mainScope {
            val (response) = safeRunIO {
                posApiService.posCashOutNotification(
                    notification,
                    "20390008",
                    "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}"
                )
            }

            assert(response?.billerReference != null)
        }
    }
}