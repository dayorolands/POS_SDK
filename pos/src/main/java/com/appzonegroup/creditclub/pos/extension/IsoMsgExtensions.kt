package com.appzonegroup.creditclub.pos.extension

import android.util.Log
import com.appzonegroup.creditclub.pos.card.NibssResponseMessage
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.appzonegroup.creditclub.pos.util.sha256String
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.extensions.hexBytes
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import java.time.Instant
import java.security.SecureRandom
import java.util.*

private const val TAG = "IsoMsgExt"

inline val ISOMsg.isSuccessful: Boolean
    get() = responseCode39 == "00"

inline val ISOMsg.hasFailed: Boolean
    get() = responseCode39 != "00"

inline val ISOMsg.responseMessage: String
    get() = NibssResponseMessage[responseCode39]

fun ISOMsg.log() = debugOnly {
    Log.d(TAG, "----ISO MESSAGE-----")
    try {
        Log.d(TAG, "  MTI : $mti")
        for (i in 1..maxField) {
            if (hasField(i)) {
                Log.d(TAG, "    Field-" + i + " : " + getString(i))
            }
        }
    } catch (e: ISOException) {
        e.printStackTrace()
    } finally {
        Log.d(TAG, "--------------------")
    }
}

fun ISOMsg.generateLog(): IsoRequestLog {

    return IsoRequestLog().apply {
        rrn = retrievalReferenceNumber37 ?: stan11 ?: ""
        uniqueId = "${terminalId41}-${rrn}-${transmissionDateTime7}"
        transactionType = processingCode3?.substring(0, 2) ?: ""
        terminalId = terminalId41 ?: ""
        amount = transactionAmount4 ?: "0"
    }
}

fun ISOMsg.prepare(sessionKeyString: String): ByteArray {
    val packedMsg = pack()

    if (maxField > 64) {
        packedMsg[35]++
    } else {
        packedMsg[19]++
    }

    val output = sessionKeyString.hexBytes + packedMsg
    val mac = output.sha256String.uppercase(Locale.getDefault())
    return packedMsg + mac.toByteArray()
}

fun ISOMsg.setTransactionTime(): Instant {
    val dateParams = TransmissionDateParams()

    transmissionDateTime7 = dateParams.transmissionDateTime
    localTransactionTime12 = dateParams.localTime
    localTransactionDate13 = dateParams.localDate


    return dateParams.date
}

fun ISOMsg.generateStan() {
    val mStan = SecureRandom().nextInt(1000)
    val mStanString = String.format("%06d", mStan)

    stan11 = mStanString
}

fun ISOMsg.init() {
    packager = ISO87Packager()
    generateStan()
    setTransactionTime()
}
