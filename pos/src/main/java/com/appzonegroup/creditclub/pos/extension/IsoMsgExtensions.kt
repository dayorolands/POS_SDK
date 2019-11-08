package com.appzonegroup.creditclub.pos.extension

import android.util.Log
import com.appzonegroup.creditclub.pos.card.NibssResponseMessage
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/10/2019.
 * Appzone Ltd
 */

private const val TAG = "IsoMsgExt"
val ISOMsg.isSuccessful get() = responseCode39 == "00"
val ISOMsg.hasFailed get() = responseCode39 != "00"

val ISOMsg.responseMessage: String
    get() = NibssResponseMessage[responseCode39]

fun ISOMsg.log() {
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
        rrn = retrievalReferenceNumber37 ?:stan11 ?: ""
        uniqueId = "${terminalId41}-${rrn}-${transmissionDateTime7}"
        transactionType = processingCode3?.substring(0, 2) ?: ""
        terminalId = terminalId41 ?: ""
        amount = transactionAmount4 ?: "0"
    }
}