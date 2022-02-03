package com.cluster.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/15/2019.
 * Appzone Ltd
 */
abstract class CardData(
    var pan: String = "",
    var holder: String = "",
    open var track2: String = "",
    var exp: String = "",
    var transactionAmount: String = "",
    var cardSequenceNumber: String = "",
    var src: String = "",
    var pinBlock: String = "",
    var cardMethod: CardReaderEvent =
        CardReaderEvent.CANCELLED,

    // Fields required by ICC transactions
    var track1: String = "",
    var authRequest: String = "",
    var cryptogram: String = "",
    var iad: String = "",
    var unpredictedNumber: String = "",
    var atc: String = "",
    var tvr: String = "",
    var transactionDate: String = "",
    var transactionType: String = "",
    var transactionCurrency: String = "",
    var aip: String = "",
    var dedicatedFileName: String = "",
    var terminalCountryCode: String = "",
    var cardHolderVerificationMethod: String = "",
    var terminalCapabilities: String = "",
    var terminalType: String = "",
    var amountOther: String = "",

    // Status fields
    var aid: String = "",
    var ksnData: String? = null,
    open var ret: Int = -1,
) {
    abstract val iccString: String?
    open var status: CardTransactionStatus? = null
        get() = field ?: CardTransactionStatus.find(ret)
        set(value) {
            field = value
            ret = value?.code ?: -1
        }
}