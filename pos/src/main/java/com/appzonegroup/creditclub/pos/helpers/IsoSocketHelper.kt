package com.appzonegroup.creditclub.pos.helpers

import android.os.Looper
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.SocketJob
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException
import java.net.ConnectException
import java.time.Instant

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/15/2019.
 * Appzone Ltd
 */
class IsoSocketHelper(
    val config: PosConfig,
    val parameters: PosParameter,
    internal val remoteConnectionInfo: RemoteConnectionInfo = config.remoteConnectionInfo
) : KoinComponent {
    private val database: PosDatabase by inject()
    private val localStorage: LocalStorage by inject()
    private val gps: TrackGPS by inject()

    @Throws(ISOException::class, IOException::class, ConnectException::class)
    inline fun sendAsync(isoMsg: BaseIsoMsg, crossinline next: (Result) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                send(isoMsg)
            }
            next(result)
        }
    }

    fun send(request: ISOMsg, isRetry: Boolean = false): Result {
        request.terminalId41 = config.terminalId

        Looper.myLooper() ?: Looper.prepare()
        val dao = database.isoRequestLogDao()

        val isoRequestLog = request.generateLog().apply {
            institutionCode = localStorage.institutionCode ?: ""
            agentCode = localStorage.agent?.agentCode ?: ""
            gpsCoordinates = gps.geolocationString ?: "0.00;0.00"
            nodeName = remoteConnectionInfo.nodeName
            if (remoteConnectionInfo is ConnectionInfo) {
                connectionInfo = remoteConnectionInfo
            }
        }

        val (response, error) = safeRun {
            val sessionKey = parameters.sessionKey
            val outputData = request.prepare(sessionKey)
            request.log()
            val output = SocketJob.execute(remoteConnectionInfo, outputData, isRetry)
            val response = ISOMsg().apply {
                packager = ISO87Packager()
                unpack(output)
            }
            response.log()

            response
        }

        if (response == null) {
            isoRequestLog.responseCode = "TE"
        } else {
            isoRequestLog.responseTime = Instant.now()
            isoRequestLog.responseCode = response.responseCode39 ?: "XX"
        }

        dao.save(isoRequestLog)

        return Result(response, error)
    }

    suspend inline fun attempt(
        request: CardIsoMsg,
        maxAttempts: Int,
        crossinline onReattempt: suspend (attempt: Int) -> Unit
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
        crossinline onReattempt: suspend (attempt: Int) -> Unit
    ): Result {
        if (maxAttempts < 2) return send(request)
        for (attempt in 1..maxAttempts) {
            if (attempt > 1) onReattempt(attempt)

            val result = send(request)
            if (attempt == maxAttempts) return result
            result.response ?: continue
            result.error ?: return result
        }

        return Result(null, null)
    }

    data class Result(val response: ISOMsg?, val error: java.lang.Exception?)
}
