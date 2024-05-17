package com.cluster.pos.card

import android.util.Log
import com.cluster.pos.PosParameter
import com.cluster.pos.extension.*
import com.cluster.pos.util.ISO87Packager
import com.cluster.pos.util.TransmissionDateParams
import org.jpos.iso.ISOMsg
import java.security.SecureRandom

fun ISOMsg.applyCardData(data: CardData): ISOMsg = apply {
    val dateParams = TransmissionDateParams()
    val stan = SecureRandom().nextInt(1000000)
    val rrnPart = SecureRandom().nextInt(100000)
    val stanString = String.format("%06d", stan)
    val rrnString = String.format("1%05d", rrnPart) + stanString

    if (packager == null) {
        packager = ISO87Packager()
    }

    pan = data.pan.trim { it <= ' ' }
    transactionAmount4 = data.transactionAmount
    transmissionDateTime7 = dateParams.transmissionDateTime
    stan11 = stanString
    localTransactionTime12 = dateParams.localTime
    localTransactionDate13 = dateParams.localDate
    cardExpirationDate14 = data.exp.substring(0, 4)
    posEntryMode22 = if (data.cardMethod == CardReaderEvent.MAG_STRIPE) "021" else if (data.cardMethod == CardReaderEvent.NFC) "071" else "051"

    cardSequenceNumber23 = if (data.cardSequenceNumber.isNotEmpty()) {
        String.format("%03d", Integer.parseInt(data.cardSequenceNumber))
    } else ""

    posConditionCode25 = "00"
    posPinCaptureCode26 = "04"
    transactionFee28 = "C00000000"
    acquiringInstIdCode32 = "636092"//"111129"
    track2Data35 = data.track2
    retrievalReferenceNumber37 = rrnString
    serviceRestrictionCode40 = data.src
    iccData55 = data.iccString
    if (data.pinBlock.isNotBlank()) {
        pinData = data.pinBlock
    }
    if (!data.ksnData.isNullOrBlank()) {
        ksnData120 = data.ksnData
    }

    posDataCode123 = run {
        if (data.cardMethod == CardReaderEvent.NFC){
            "A10101711344101"
        } else {
            val tmpPosDataCode = StringBuilder("510101511344101")
            if (data.holder.isNotEmpty()) {
                tmpPosDataCode[4] = '0'
            }
            if (data.cardMethod == CardReaderEvent.MAG_STRIPE) {
                tmpPosDataCode[6] = '2'
            }
            tmpPosDataCode.toString()
        }
    }
}

fun ISOMsg.applySalesCompleteData(data: CardData) : ISOMsg = apply {
    val dateParams = TransmissionDateParams()
    val stan = SecureRandom().nextInt(1000000)
    val rrnPart = SecureRandom().nextInt(100000)
    val stanString = String.format("%06d", stan)
    val rrnString = String.format("1%05d", rrnPart) + stanString

    if (packager == null) {
        packager = ISO87Packager()
    }

    pan = data.pan.trim { it <= ' ' }
    transactionAmount4 = data.transactionAmount
    transmissionDateTime7 = dateParams.transmissionDateTime
    stan11 = stanString
    localTransactionTime12 = dateParams.localTime
    localTransactionDate13 = dateParams.localDate
    cardExpirationDate14 = data.exp.substring(0, 4)
    posEntryMode22 = if (data.cardMethod == CardReaderEvent.MAG_STRIPE) "021" else "051"

    cardSequenceNumber23 = if (data.cardSequenceNumber.isNotEmpty()) {
        String.format("%03d", Integer.parseInt(data.cardSequenceNumber))
    } else ""

    posConditionCode25 = "00"
    posPinCaptureCode26 = "04"
    transactionFee28 = "C00000000"
    acquiringInstIdCode32 = "636092"//"111129"
    track2Data35 = data.track2
    retrievalReferenceNumber37 = rrnString
    serviceRestrictionCode40 = data.src
    iccData55 = data.iccString

    posDataCode123 = run {
        val tmpPosDataCode = StringBuilder("510101511344101")
        if (data.holder.isNotEmpty()) {
            tmpPosDataCode[4] = '0'
        }
        if (data.cardMethod == CardReaderEvent.MAG_STRIPE) {
            tmpPosDataCode[6] = '2'
        }
        tmpPosDataCode.toString()
    }
}

fun ISOMsg.generateReversal(cardData: CardData): ISOMsg {
    val financialMessage = this
    val elements = "0200$stan11$transmissionDateTime7" +
            "${acquiringInstIdCode32?.padStart(11, '0')}" +
            "${forwardingInstIdCode33?.padStart(11, '0')}"

    return ISOMsg().apply {
        packager = ISO87Packager()
        generateStan()
        setTransactionTime()
        applyCardData(cardData)
        mti = "0420"
        processingCode3 = "000000"
        transactionAmount4 = financialMessage.transactionAmount4
        replacementAmounts95 =
            "${transactionAmount4?.padStart(12, '0')}000000000000" +
                    "${transactionFee28?.padStart(9, '0')}000000000"

        originalDataElements90 = elements
        stan11 = financialMessage.stan11
        localTransactionDate13 = financialMessage.localTransactionDate13
        localTransactionTime12 = financialMessage.localTransactionTime12
        retrievalReferenceNumber37 = financialMessage.retrievalReferenceNumber37

        processingCode3 = financialMessage.processingCode3
        messageReasonCode56 = "4000"
        cardAcceptorIdCode42 = financialMessage.cardAcceptorIdCode42
        cardAcceptorNameLocation43 = financialMessage.cardAcceptorNameLocation43
        currencyCode49 = financialMessage.currencyCode49
        merchantType18 = financialMessage.merchantType18
    }
}

fun ISOMsg.applyManagementData(params: PosParameter.ManagementData) {
    cardAcceptorIdCode42 = params.cardAcceptorId
    cardAcceptorNameLocation43 = params.cardAcceptorLocation
    currencyCode49 = params.countryCode
    merchantType18 = params.merchantCategoryCode
}