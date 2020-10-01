package com.creditclub.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/15/2019.
 * Appzone Ltd
 */
abstract class CardData {
    abstract val iccString: String?

    var pan: String = ""
    var holder: String = ""
    open var track2: String = ""
    var exp: String = ""
    var transactionAmount: String = ""
    var cardSequenceNumber: String = ""
    var src: String = ""
    var pinBlock: String = ""
    var cardMethod: CardReaderEvent =
        CardReaderEvent.CANCELLED

    // Fields required by ICC transactions
    var track1: String = ""
    var authRequest: String = ""
    var cryptogram: String = ""
    var iad: String = ""
    var unpredictedNumber: String = ""
    var atc: String = ""
    var tvr: String = ""
    var transactionDate: String = ""
    var transactionType: String = ""
    var transactionCurrency: String = ""
    var aip: String = ""
    var dedicatedFileName: String = ""
    var terminalCountryCode: String = ""
    var cardHolderVerificationMethod: String = ""
    var terminalCapabilities: String = ""
    var terminalType: String = ""
    var amountOther: String = ""

    // Status fields
    var aid: String = ""
    var ksnData: String? = null
    open var ret: Int = -1
    open var status: CardTransactionStatus? = null
        get() = CardTransactionStatus.find(ret)
        set(value) {
            field = value
            ret = value?.code ?: -1
        }

    val type: String
        get() = when (aid) {
            "A0000000032020" -> "VISA"
            // "A0000000031010" -> "VISA Debit Credit Classic"
            "A0000000031010" -> "VISA Debit"
            // "A0000000031010" -> "VISA Credit"
            // "A0000000032010" -> "VISA Electron"
            "A0000004540010" -> "Etranzact Genesis Card"
            "A0000004540011" -> "Etranzact Genesis Card"
            "A0000000042203" -> "MasterCard US"
            "A0000000041010" -> "MasterCard"
            "A0000000042010" -> "MasterCard Specific"
            "A0000000043010" -> "MasterCard Specific"
            "A0000000045010" -> "MasterCard Specific"
            "A0000003710001" -> "InterSwitch Verve Card"
            else -> "Unknown Card"
        }
}