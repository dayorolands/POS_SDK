package com.cluster.pos.helpers

import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContentProviderCompat.requireContext
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.data.response.isSuccessful
import com.cluster.core.util.SafeRunResult
import com.cluster.core.util.safeRun
import com.cluster.pos.PosConfig
import com.cluster.pos.PosParameter
import com.cluster.pos.RemoteConnectionInfo
import com.cluster.pos.card.isoResponseMessage
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.extension.*
import com.cluster.pos.model.ConnectionInfo
import com.cluster.pos.util.ISO87Packager
import com.cluster.pos.util.SocketJob
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import org.jpos.iso.ISOMsg
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
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
    private var firebaseAnalytics = FirebaseAnalytics.getInstance(get())
        private set

    fun send(request: ISOMsg, isRetry: Boolean = false): SafeRunResult<ISOMsg> {

        var derivedTimeout = remoteConnectionInfo.timeout
        if (isRetry) {
            derivedTimeout = remoteConnectionInfo.requeryConfig?.timeout ?: derivedTimeout
        }

        if (request.packager == null) {
            request.packager = ISO87Packager()
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

        firebaseAnalytics.logEvent("nibss_iso_req_log", Bundle().apply {
            this.putString("processing_code", request.processingCode3)
            this.putString("trans_amount", request.transactionAmount4)
            this.putString("trans_date_time", request.transmissionDateTime7)
            this.putString("stan", request.stan11)
            this.putString("local_time", request.localTransactionTime12)
            this.putString("local_date", request.localTransactionDate13)
            this.putString("exp_date", request.cardExpirationDate14)
            this.putString("merchant_type", request.merchantType18)
            this.putString("pos_entry_mode", request.posEntryMode22)
            this.putString("card_sequence_no", request.cardSequenceNumber23)
            this.putString("pos_cond_code", request.posConditionCode25)
            this.putString("transaction_fee", request.transactionFee28)
            this.putString("acquiring_id", request.acquiringInstIdCode32)
            this.putString("rrn", request.retrievalReferenceNumber37)
            this.putString("src", request.serviceRestrictionCode40)
            this.putString("terminal_id", request.terminalId41)
            this.putString("card_acceptor_id", request.cardAcceptorIdCode42)
            this.putString("card_acceptor_location", request.cardAcceptorNameLocation43)
            this.putString("pinblock", request.pinData)
            this.putString("pos_data_code", request.posDataCode123)
        })

        val result = safeRun {
            val sessionKey = parameters.sessionKey
            val outputData = request.prepare(sessionKey)
            request.log()
            val output = SocketJob.execute(remoteConnectionInfo, outputData, derivedTimeout)
            val response = ISOMsg().apply {
                packager = ISO87Packager()
                unpack(output)
            }

            firebaseAnalytics.logEvent("nibss_iso_resp_log", Bundle().apply {
                this.putString("response_code", response.responseCode39)
            })

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
        request: ISOMsg,
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

    fun maskedPan(pan: String?):String{
        val first6 = pan?.substring(1,6)
        val last4 = pan?.takeLast(4)
        val finalString = first6 + "*********" + last4

        return finalString
    }
}
