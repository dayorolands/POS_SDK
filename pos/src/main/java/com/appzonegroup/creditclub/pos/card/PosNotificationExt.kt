package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.PosConfig
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun PosApiService.logPosNotification(
    db: PosDatabase,
    backendConfig: BackendConfig,
    posConfig: PosConfig,
    posNotification: PosNotification
) {
    val (response) = safeRunIO {
        posCashOutNotification(
            posNotification,
            "iRestrict ${backendConfig.posNotificationToken}",
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