package com.appzonegroup.creditclub.pos.work

import android.content.Context
import android.os.Bundle
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.extension.responseCode39
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.inject

class ReversalWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val config: PosConfig by inject()
        val parameters: PosParameter by inject()
        val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(applicationContext) }
        val isoSocketHelper by lazy { IsoSocketHelper(config, parameters) }
        val localStorage: LocalStorage by inject()

        val reversalDao = posDatabase.reversalDao()

        val jobs = reversalDao.all().map { reversal ->
            async {
                val request = reversal.isoMsg
                val (response) = isoSocketHelper.send(request)

                if (response != null) {
                    firebaseAnalytics.logEvent("pos_reversal", Bundle().apply {
                        putString("terminal_id", config.terminalId)
                        putString("rrn", request.retrievalReferenceNumber37)
                        putString("stan", request.stan11)
                        putString("agent_code", localStorage.agent?.agentCode)
                        putString("agent_phone", localStorage.agentPhone)
                        putString("institution_code", localStorage.institutionCode)
                        putString("response_code", response.responseCode39)
                    })

                    reversalDao.delete(reversal)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}