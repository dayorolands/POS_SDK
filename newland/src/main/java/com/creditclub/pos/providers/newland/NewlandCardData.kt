package com.creditclub.pos.providers.newland

import com.cluster.pos.card.CardData

internal class NewlandCardData: CardData() {
    override var track2 = ""
        set(value) {
            field = if (value.length > 37) {
                value.substring(0, 37)
            } else value
        }

    internal var mIccString = ""

    override val iccString: String get() = mIccString

    override fun toString(): String {
        return "CardData: {ret=$ret, transactionAmount=$transactionAmount, exp=$exp, holder=$holder, " +
                "cardSequenceNumber=$cardSequenceNumber, aid=$aid, track2=$track2, atc=$atc, " +
                "cryptogramInformationData=$cryptogramInformationData, terminalCapabilities=$terminalCapabilities, " +
                "terminalType=$terminalType, iad=$iad, tvr=$tvr, unpredictedNumber=$unpredictedNumber, " +
                "dedicatedFileName=$dedicatedFileName, transactionDate=$transactionDate, " +
                "transactionType=$transactionType, transactionCurrency=$transactionCurrency, " +
                "cardHolderVerificationMethod=$cardHolderVerificationMethod, amountAuthorized=$amountAuthorized, " +
                "aip=$aip, cryptogram=$cryptogram}"
    }
}