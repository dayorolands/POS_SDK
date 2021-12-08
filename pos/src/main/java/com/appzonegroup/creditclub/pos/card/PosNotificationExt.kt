package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.cluster.core.data.api.AppConfig
import com.cluster.core.util.safeRunIO
import com.cluster.pos.PosConfig
import com.cluster.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun PosApiService.logPosNotification(
    db: PosDatabase,
    appConfig: AppConfig,
    posConfig: PosConfig,
    posNotification: PosNotification
) {
    val (response) = safeRunIO {
        posCashOutNotification(
            posNotification,
            "iRestrict ${appConfig.posNotificationToken}",
            posConfig.terminalId
        )
    }

    response ?: return

    if (!response.billerReference.isNullOrBlank()) {
        withContext(Dispatchers.IO) {
            db.posNotificationDao().delete(posNotification.id)
        }
    }
}