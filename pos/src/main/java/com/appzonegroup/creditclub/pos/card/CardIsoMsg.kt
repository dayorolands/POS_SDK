package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import org.jpos.iso.ISOException
import org.jpos.transaction.TransactionManager
import org.json.JSONException
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*

open class CardIsoMsg : BaseIsoMsg() {
    var acquiringInstIdCode32: String?
        get() = getString(32)
        set(value) = set(32, value)

    var forwardingInstIdCode33: String?
        get() = getString(33)
        set(value) = set(33, value)

    var additionalAmounts54: String?
        get() = getString(54)
        set(value) = set(54, value)

    var cardAcceptorIdCode42: String?
        get() = getString(42)
        set(value) = set(42, value)

    var cardAcceptorNameLocation43: String?
        get() = getString(43)
        set(value) = set(43, value)

    var cardExpirationDate14: String?
        get() = getString(14)
        set(value) = set(14, value)

    var cardSequenceNumber23: String?
        get() = getString(23)
        set(value) = set(23, value)

    var currencyCode49: String?
        get() = getString(49)
        set(value) = set(49, value)

    var iccData55: String?
        set(value) = set(55, value)
        get() = getString(55)?.replace(" ", TransactionManager.DEFAULT_GROUP)

    var merchantType18: String?
        get() = getString(18)
        set(value) = set(18, value)

    var messageReasonCode56: String?
        get() = getString(56)
        set(value) = set(56, value)

    var pan: String?
        get() = getString(2)
        set(value) = set(2, value)

    var paymentInformation: String?
        get() = getString(60)
        set(value) = set(60, value)

    var pinData: String?
        get() = getString(52)
        set(value) = set(52, value)

    var posConditionCode25: String?
        get() = getString(25)
        set(value) = set(25, value)

    var posEntryMode22: String?
        get() = getString(22)
        set(value) = set(22, value)

    var posPinCaptureCode26: String?
        get() = getString(26)
        set(value) = set(26, value)

    var secondaryHashValue: String?
        get() = getString(128)
        set(value) = set(128, value)

    var securityRelatedInformation: String?
        get() = getString(53)
        set(value) = set(53, value)

    var serviceRestrictionCode40: String?
        get() = getString(40)
        set(value) = set(40, value)

    var track2Data35: String?
        get() = getString(35)?.replace("?", TransactionManager.DEFAULT_GROUP)?.replace(
            ";",
            TransactionManager.DEFAULT_GROUP
        )?.replace("=", "D")
        set(value) = set(35, value)

    var transactionAmount4: String?
        get() = getString(4)
        set(value) = set(4, value)

    var transactionFee28: String?
        get() = getString(28)
        set(value) = set(28, value)

    var settlementFee29: String?
        get() = getString(29)
        set(value) = set(29, value)

    var transportData: String?
        get() = getString(59)
        set(value) = set(59, value)

    var managementDataOne: String?
        get() = getString(62)
        set(value) = set(62, value)

    var posDataCode123: String?
        get() = getString(123)
        set(value) = set(123, value)

    fun generateRRN() {
        val rrn = SecureRandom().nextInt(1000000)
        val rrnString = String.format("%012d", rrn)

        retrievalReferenceNumber37 = rrnString
    }

    @Throws(
        ISOException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        JSONException::class
    )
    open fun apply(data: CardData): CardIsoMsg = apply {
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
        if (!data.pinBlock.isNullOrBlank()) pinData = data.pinBlock

        run {
            //            val tmpPosDataCode = StringBuilder("511501513344101")
            val tmpPosDataCode = StringBuilder("510101511344101")
            if (data.holder.isNotEmpty()) tmpPosDataCode[4] = '0'
            if (data.cardMethod == CardReaderEvent.MAG_STRIPE) tmpPosDataCode[6] = '2'
            posDataCode123 = tmpPosDataCode.toString()
        }
    }

    fun withParameters(params: ParameterService.ParameterObject) {
        cardAcceptorIdCode42 = params.cardAcceptorId
        cardAcceptorNameLocation43 = params.cardAcceptorLocation
        currencyCode49 = params.countryCode
        merchantType18 = params.merchantCategoryCode
    }

//    fun withPinData(pinBlock:String, parameterService: ParameterService) {
//        if (pinBlock.isNotEmpty()) {
//            val tripleDesCipher = TripleDesCipher(StringUtil.hexStringToByte(parameterService.pinKey))
//            val encryptedPinBlock = tripleDesCipher.encrypt(StringUtil.hexStringToByte(pinBlock))
//            pinData = StringUtil.bytesToHexString(encryptedPinBlock)
//        }
//    }

    val additionalAmount: String
        get() = try {
            additionalAmounts54?.substring(8, 20) ?: "0"
        } catch (ex: Exception) {
            "0"
        }
}