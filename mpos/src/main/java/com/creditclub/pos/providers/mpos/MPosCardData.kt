package com.creditclub.pos.providers.mpos

import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.extensions.hexBytes


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardData internal constructor(
    override var ret: Int,
    map: Map<String, String>,
    keyController: KeyController?
) : CardData() {

    override val iccString: String? = keyController?.run { decrypt(map["ICData55"]) }

    init {
        if (keyController != null) {
            iccString
            transactionAmount = map["Amount"]?.replace(".", "") ?: "0"
            cardSequenceNumber = map["PanSeqNo"] ?: ""

            track2 = keyController.decrypt(map["EncryptTrack2"]) ?: ""

            exp = map["ExpireDate"] ?: ""
            populateFromTrack2()

            cardMethod = when (map["SzEntryMode"]) {
                "0" -> CardReaderEvent.MAG_STRIPE
                "1" -> CardReaderEvent.CHIP
                else -> CardReaderEvent.CANCELLED
            }

//            pinBlock = map["PINBlock"]?.run {
//                keyController.decrypt(TerminalUtils.hexStringToByteArray(this))
//            } ?: "00000000"
        }
    }

    private fun KeyController.decrypt(encryptedValue: String?): String? {
        return decrypt(encryptedValue?.hexBytes)
    }

    private fun populateFromTrack2() {
        val fieldArray = track2.split('=', 'd', ignoreCase = true)

        pan = fieldArray[0].substring(2)
        holder = ""
        exp = fieldArray[1].substring(0, 4)
        src = fieldArray[1].substring(11, 14)
    }
}