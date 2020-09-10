package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.models.messaging.ReversalRequest
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOMsg
import java.security.SecureRandom

fun ISOMsg.applyCardData(data: CardData): ISOMsg = apply {
    val dateParams = TransmissionDateParams()
    val stan = SecureRandom().nextInt(1000000)
    val rrnPart = SecureRandom().nextInt(100000)
    val stanString = String.format("%06d", stan)
    val rrnString = String.format("1%05d", rrnPart) + stanString

    packager = ISO87Packager()

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
    if (!data.pinBlock.isNullOrBlank()) {
        pinData = data.pinBlock
    }
    if(!data.ksnData.isNullOrBlank()){
        ksnData120 = data.ksnData
    }

    run {
        //            val tmpPosDataCode = StringBuilder("511501513344101")
        val tmpPosDataCode = StringBuilder("510101511344101")
        if (data.holder.isNotEmpty()) tmpPosDataCode[4] = '0'
        if (data.cardMethod == CardReaderEvent.MAG_STRIPE) tmpPosDataCode[6] = '2'
        posDataCode123 = tmpPosDataCode.toString()
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
        messageReasonCode56 = "4021"
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