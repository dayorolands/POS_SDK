package com.cluster.pos.helpers

import android.os.Looper
import com.cluster.pos.card.CardIsoMsg
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.extension.*
import com.cluster.pos.util.ISO87Packager
import com.cluster.pos.util.SocketJob
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.SafeRunResult
import com.cluster.core.util.safeRun
import com.cluster.pos.PosConfig
import com.cluster.pos.PosParameter
import com.cluster.pos.RemoteConnectionInfo
import com.cluster.pos.model.ConnectionInfo
import org.jpos.iso.ISOMsg
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.Instant

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/15/2019.
 * Appzone Ltd
 */
class IsoSocketHelper(
    val config: PosConfig,
    val parameters: PosParameter,
    internal val remoteConnectionInfo: RemoteConnectionInfo = config.remoteConnectionInfo,
) : KoinComponent {
    private val database: PosDatabase by inject()
    private val localStorage: LocalStorage by inject()

    fun send(request: ISOMsg, isRetry: Boolean = false): SafeRunResult<ISOMsg> {
        var derivedTimeout = remoteConnectionInfo.timeout
        if (isRetry) {
            derivedTimeout = remoteConnectionInfo.requeryConfig?.timeout ?: derivedTimeout
        }

        request.terminalId41 = config.terminalId

        Looper.myLooper() ?: Looper.prepare()
        val dao = database.isoRequestLogDao()

        val isoRequestLog = request.generateLog().apply {
            institutionCode = localStorage.institutionCode ?: ""
            agentCode = localStorage.agent?.agentCode ?: ""
            gpsCoordinates = localStorage.lastKnownLocation
            nodeName = remoteConnectionInfo.nodeName
            if (remoteConnectionInfo is ConnectionInfo) {
                connectionInfo = remoteConnectionInfo
            }
        }

        val result = safeRun {
            val sessionKey = parameters.sessionKey
            val outputData = request.prepare(sessionKey)
            request.log()
            val output = SocketJob.execute(remoteConnectionInfo, outputData, derivedTimeout)
            val response = ISOMsg().apply {
                packager = ISO87Packager()
                unpack(output)
            }
            response.log()

            response
        }
        val (response) = result

        if (response == null) {
            isoRequestLog.responseCode = "TE"
        } else {
            isoRequestLog.responseTime = Instant.now()
            isoRequestLog.responseCode = response.responseCode39 ?: "XX"
        }
        isoRequestLog.duration = Duration.between(
            isoRequestLog.requestTime,
            isoRequestLog.responseTime ?: Instant.now()
        ).toMillis()

        dao.save(isoRequestLog)

        return result
    }

    suspend inline fun attempt(
        request: CardIsoMsg,
        maxAttempts: Int,
        crossinline onReattempt: suspend (attempt: Int) -> Unit,
    ): Boolean {
        if (maxAttempts < 2) {
            send(request).error ?: return true
            return false
        }
        for (attempt in 1..maxAttempts) {
            if (attempt > 1) onReattempt(attempt)

            val (response, error) = send(request)

            response ?: continue
            error ?: return true
        }

        return false
    }

    suspend inline fun send(
        request: ISOMsg,
        maxAttempts: Int,
        crossinline onReattempt: suspend (attempt: Int) -> Unit,
    ): SafeRunResult<ISOMsg> {
        if (maxAttempts < 2) return send(request)
        for (attempt in 1..maxAttempts) {
            if (attempt > 1) onReattempt(attempt)

            val result = send(request)
            if (attempt == maxAttempts) return result
            result.data ?: continue
            result.error ?: return result
        }

        return SafeRunResult(null)
    }
}
