package com.appzonegroup.creditclub.pos.helpers

import android.content.Context
import android.os.Looper
import android.util.Log
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.generateLog
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.SocketJob
import com.appzonegroup.creditclub.pos.util.TerminalUtils
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.safeRun
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
    val config: ConfigService,
    val parameters: ParameterService,
    context: Context
) : KoinComponent {
    private val tag = IsoSocketHelper::class.java.simpleName

    private val database: PosDatabase by inject()
    private val localStorage: LocalStorage by inject()
    private val gps: TrackGPS by inject()

    @Throws(ISOException::class, IOException::class, ConnectException::class)
    fun sendAsync(isoMsg: BaseIsoMsg, next: (Result) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) {
                send(isoMsg)
            }
            next(result)
        }
    }

    fun open(block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            block()
        }
    }

    fun send(request: BaseIsoMsg): Result {
        Looper.myLooper() ?: Looper.prepare()
        val dao = database.isoRequestLogDao()

        val isoRequestLog = request.generateLog().apply {
            institutionCode = localStorage.institutionCode ?: ""
            agentCode = localStorage.agent?.agentCode ?: ""
            gpsCoordinates = gps.geolocationString
        }

        val (response, error) = safeRun {
            request.terminalId41 = config.terminalId
            val sessionKey = parameters.sessionKey
            val outputData = request.prepare(sessionKey)
            request.dump(System.out, "REQUEST")
            Log.d(tag, "RESULT : " + String(outputData))
            val output =
                SocketJob.sslSocketConnectionJob(config.posMode.ip, config.posMode.port, outputData)

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

        isoRequestLog.responseTime = Instant.now()

        if (response == null) {
            isoRequestLog.responseCode = "TE"
        } else isoRequestLog.responseCode = response.responseCode39 ?: "XX"

        dao.save(isoRequestLog)

        return Result(response, error)
    }

    suspend fun attempt(
        request: CardIsoMsg,
        maxAttempts: Int,
        onReattempt: suspend (attempt: Int) -> Unit
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
