package com.cluster.work

import android.content.Context
import android.os.Bundle
import androidx.work.WorkerParameters
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.pos.Platform
import com.cluster.pos.PosConfig
import com.cluster.pos.PosParameter
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.extension.responseCode39
import com.cluster.pos.extension.retrievalReferenceNumber37
import com.cluster.pos.extension.stan11
import com.cluster.pos.helpers.IsoSocketHelper
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class ReversalWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val config: PosConfig by inject()
        val parameters: PosParameter by inject()
        val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(applicationContext) }
        val isoSocketHelper by lazy {
            IsoSocketHelper(
                config = config,
                parameters = parameters,
            )
        }
        val localStorage: LocalStorage by inject()

        val posDatabase: PosDatabase by inject()

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