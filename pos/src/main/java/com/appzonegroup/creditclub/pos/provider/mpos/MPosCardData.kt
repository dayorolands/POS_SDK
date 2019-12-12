package com.appzonegroup.creditclub.pos.provider.mpos

import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.CardReaderEvent
import com.appzonegroup.creditclub.pos.util.TerminalUtils


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardData(
    override var ret: Int,
    map: Map<String, String>,
    keyController: KeyController
) : CardData() {

    override val iccString: String? by lazy { map.decryptedValue(keyController, "ICData55") }

    init {
        transactionAmount = map["Amount"]?.replace(".", "") ?: "0"
        cardSequenceNumber = map["PanSeqNo"] ?: ""
//        val downgrad = map["Downgrade"] // card mode ==0 normal 1: downgrade
//        val encryptTrack3 = map["EncryptTrack3"] // encrypted track 3  data

        track2 = map.decryptedValue(keyController, "EncryptTrack2")

        val szentryMode = map["SzEntryMode"] // encryption mode 22 field

        exp = map["ExpireDate"] ?: ""
        pan = fromTrack2("")

//        cardMethod = when (map["CardType"]) {
//            "0" -> CardReaderEvent.MAG_STRIPE
//            "1" -> CardReaderEvent.CHIP
//            else -> CardReaderEvent.CANCELLED
//        }

        cardMethod = if (ret == 1) CardReaderEvent.CHIP else CardReaderEvent.CANCELLED

        pinBlock = map["PINBlock"]?.run {
            keyController.decrypt(TerminalUtils.hexStringToByteArray(this))
        } ?: "00000000"
    }

    private fun Map<String, String>.decryptedValue(
        keyController: KeyController,
        key: String
    ): String {
        val byteArray = TerminalUtils.hexStringToByteArray(this[key])
        return keyController.decrypt(byteArray) ?: ""
    }

    private fun fromTrack2(tag: String): String {
        TODO("Extract $tag value from track2")
    }
}