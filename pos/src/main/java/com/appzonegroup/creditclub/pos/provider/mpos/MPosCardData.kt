package com.appzonegroup.creditclub.pos.provider.mpos

import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.CardReaderEvent


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardData(override var ret: Int, val map: Map<String, String>) : CardData() {

    override val iccString: String? = map["ICData55"]

    init {
        transactionAmount = map["Amount"]?.replace(".", "") ?: "0"
        cardSequenceNumber = map["PanSeqNo"] ?: ""
        val downgrad = map["Downgrade"] // card mode ==0 normal 1: downgrade
        val encryptTrack3 = map["EncryptTrack3"] // encrypted track 3  data
        val encryptTrack2 = map["EncryptTrack2"] // encrypted track 2 data
        val szentryMode = map["SzEntryMode"] // encryption mode 22 field

        exp = map["ExpireDate"] ?: ""

        cardMethod = when (map["CardType"]) {
            "0" -> CardReaderEvent.MAG_STRIPE
            "1" -> CardReaderEvent.CHIP
            else -> CardReaderEvent.CANCELLED
        }

        pinBlock = map["PINBlock"] ?: "00000000"
    }
}