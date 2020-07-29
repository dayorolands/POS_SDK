package com.appzonegroup.creditclub.pos.helpers

import android.content.Context
import android.os.Looper
import android.util.Log
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.generateLog
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.SocketJob
import com.appzonegroup.creditclub.pos.util.TerminalUtils
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import kotlinx.coroutines.*
import org.jpos.iso.ISOException
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.Instant
import java.io.IOException
import java.net.ConnectException

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/15/2019.
 * Appzone Ltd
 */
class IsoSocketHelper(
    val config: PosConfig,
    val parameters: PosParameter,
    context: Context
) : KoinComponent {
    private val tag = IsoSocketHelper::class.java.simpleName

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

    inline fun open(crossinline block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            block()
        }
    }

    fun send(request: BaseIsoMsg): Result {
        request.terminalId41 = config.terminalId

        Looper.myLooper() ?: Looper.prepare()
        val dao = database.isoRequestLogDao()

        val isoRequestLog = request.generateLog().apply {
            institutionCode = localStorage.institutionCode ?: ""
            agentCode = localStorage.agent?.agentCode ?: ""
            gpsCoordinates = gps.geolocationString ?: "0.00;0.00"
        }

        val (response, error) = safeRun {
            val sessionKey = parameters.sessionKey
            val outputData = request.prepare(sessionKey)
            request.dump(System.out, "REQUEST")
            Log.d(tag, "RESULT : " + String(outputData))
            val output =
                SocketJob.sslSocketConnectionJob(
                    config.remoteConnectionInfo.ip,
                    config.remoteConnectionInfo.port,
                    outputData
                )

            println("MESSAGE: " + String(output!!))

            val response = BaseIsoMsg()
            response.packager = ISO87Packager()
            response.unpack(output)
            TerminalUtils.logISOMsg(response)

            if (response.getString(39) != "00") {
                Log.d(tag, "Error contacting Nibss server")
            } else {
                Log.d(tag, "Successful Call to Nibss")
            }

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
        for (attempt in 1..maxAttempts) {
            if (attempt > 1) onReattempt(attempt)

            val (response, error) = send(request)

            response ?: continue
            error ?: return true
        }

        return false
    }

    data class Result(val response: BaseIsoMsg?, val error: java.lang.Exception?)
}
