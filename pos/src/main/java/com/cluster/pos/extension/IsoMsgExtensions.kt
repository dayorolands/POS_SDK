package com.cluster.pos.extension

import com.cluster.core.util.debugOnly
import com.cluster.pos.card.isoResponseMessage
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.models.IsoRequestLog
import com.cluster.pos.util.ISO87Packager
import com.cluster.pos.util.TransmissionDateParams
import com.cluster.pos.util.sha256String
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import java.security.SecureRandom
import java.time.Instant
import java.util.*

inline val ISOMsg.isSuccessful: Boolean
    get() = responseCode39 == "00"

inline val ISOMsg.hasFailed: Boolean
    get() = responseCode39 != "00"

inline val ISOMsg.responseMessage: String
    get() = isoResponseMessage(responseCode39)

fun ISOMsg.log() = debugOnly {
    println("----ISO MESSAGE-----")
    try {
        println("  MTI : $mti")
        for (i in 1..maxField) {
            if (hasField(i)) {
                println("    Field-" + i + " : " + getString(i))
            }
        }
    } catch (e: ISOException) {
        e.printStackTrace()
    } finally {
        println("--------------------")
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
