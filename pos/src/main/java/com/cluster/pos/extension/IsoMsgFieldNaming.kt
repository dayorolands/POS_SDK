package com.cluster.pos.extension

import org.jpos.iso.ISOMsg
import org.jpos.transaction.TransactionManager
import kotlin.reflect.KProperty


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

@JvmInline
value class IsoMsgFieldDelegate(private val fieldNo: Int) {
    operator fun getValue(isoMsg: ISOMsg, property: KProperty<*>): String? {
        return isoMsg.getString(fieldNo)
    }

    operator fun setValue(isoMsg: ISOMsg, property: KProperty<*>, value: String?) {
        isoMsg.set(fieldNo, value)
    }
}

fun isoMsgField(fldno: Int) = IsoMsgFieldDelegate(fldno)

var ISOMsg.pan by isoMsgField(2)

var ISOMsg.processingCode3 by isoMsgField(3)

var ISOMsg.transactionAmount4 by isoMsgField(4)

var ISOMsg.transmissionDateTime7 by isoMsgField(7)

var ISOMsg.stan11 by isoMsgField(11)

var ISOMsg.localTransactionTime12 by isoMsgField(12)

var ISOMsg.localTransactionDate13 by isoMsgField(13)

var ISOMsg.cardExpirationDate14 by isoMsgField(14)

var ISOMsg.merchantType18 by isoMsgField(18)

var ISOMsg.posEntryMode22 by isoMsgField(22)

var ISOMsg.cardSequenceNumber23 by isoMsgField(23)

var ISOMsg.posConditionCode25 by isoMsgField(25)

var ISOMsg.transactionFee28 by isoMsgField(28)

var ISOMsg.settlementFee29 by isoMsgField(29)

var ISOMsg.acquiringInstIdCode32 by isoMsgField(32)

var ISOMsg.forwardingInstIdCode33 by isoMsgField(33)

var ISOMsg.track2Data35
    get() = getString(35)
        ?.replace("?", TransactionManager.DEFAULT_GROUP)
        ?.replace(";", TransactionManager.DEFAULT_GROUP)
        ?.replace("=", "D")
    set(value) = set(35, value)

var ISOMsg.retrievalReferenceNumber37 by isoMsgField(37)

var ISOMsg.responseCode39 by isoMsgField(39)

var ISOMsg.serviceRestrictionCode40 by isoMsgField(40)

var ISOMsg.terminalId41 by isoMsgField(41)

var ISOMsg.cardAcceptorIdCode42 by isoMsgField(42)

var ISOMsg.cardAcceptorNameLocation43 by isoMsgField(43)

var ISOMsg.currencyCode49 by isoMsgField(49)

var ISOMsg.securityRelatedInformation by isoMsgField(53)

var ISOMsg.pinData by isoMsgField(52)

var ISOMsg.additionalAmounts54 by isoMsgField(54)

var ISOMsg.iccData55
    get() = getString(55)?.replace(" ", TransactionManager.DEFAULT_GROUP)
    set(value) = set(55, value)

var ISOMsg.messageReasonCode56 by isoMsgField(56)

var ISOMsg.paymentInformation by isoMsgField(60)

var ISOMsg.posPinCaptureCode26 by isoMsgField(26)

var ISOMsg.transportData by isoMsgField(59)

var ISOMsg.managementDataOne62 by isoMsgField(62)

var ISOMsg.managementDataTwo63 by isoMsgField(63)

var ISOMsg.originalDataElements90 by isoMsgField(90)

var ISOMsg.replacementAmounts95 by isoMsgField(95)

var ISOMsg.ksnData120 by isoMsgField(120)

var ISOMsg.posDataCode123 by isoMsgField(123)

var ISOMsg.secondaryHashValue by isoMsgField(128)

