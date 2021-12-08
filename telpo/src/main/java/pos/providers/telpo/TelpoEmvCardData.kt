package pos.providers.telpo

import android.util.Log
import com.cluster.core.util.debugOnly
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.telpo.emv.EmvService
import com.telpo.emv.EmvTLV
import com.telpo.emv.util.hexString

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/15/2019.
 * Appzone Ltd
 */
class TelpoEmvCardData constructor(
    private val emvService: EmvService? = null,
    magStripData: Array<String?>? = null
) : CardData() {

    override val iccString: String?
        get() {
            if (emvService != null) {
                var iccData = appendMeta("9F26", authRequest)
                iccData += appendMeta("9F27", cryptogram)
                iccData += appendMeta("9F10", iad)
                iccData += appendMeta("9F37", unpredictedNumber)
                iccData += appendMeta("9F36", atc)
                iccData += appendMeta("95", tvr)
                iccData += appendMeta("9A", transactionDate)
                iccData += appendMeta("9C", transactionType)
                iccData += appendMeta("9F02", transactionAmount)
                iccData += appendMeta("5F2A", transactionCurrency)
                iccData += appendMeta("82", aip)
                iccData += appendMeta("84", dedicatedFileName)
                iccData += appendMeta("9F1A", terminalCountryCode)
                iccData += appendMeta("9F34", cardHolderVerificationMethod)
                iccData += appendMeta("9F33", terminalCapabilities)
                iccData += appendMeta("9F35", terminalType)
                iccData += appendMeta("9F03", amountOther)
                return iccData
            }

            return null
        }

    override var track2 = ""
        set(value) {
            field = if (value.length > 37) {
                value.substring(0, 37)
            } else value
        }

    init {
        emvService?.run {
            authRequest = getValue(0x9F26)
            cryptogram = getValue(0x9F27)
            debugOnly { Log.d("TelpoEmvCardData", "Cryptogram: $cryptogram") }
            iad = getValue(0x9F10)
            unpredictedNumber = getValue(0x9F37)
            atc = getValue(0x9F36)
            tvr = getValue(0x95)
            transactionDate = getValue(0x9A)
            transactionType = getValue(0x9C)
            //            transactionCurrency = getValue(0x5F2A)
            pan = getValue(0x5A, hex = false, fpadded = true)
            track2 = getValue(0x57, hex = false, fpadded = true)
            populateSRC(track2)
            exp = getValue(0x5F24)
            aip = getValue(0x82)
            dedicatedFileName = getValue(0x84)
            holder = getValue(0x5F20, true)
            //terminalCountryCode = getValue(0x9F1A)
            cardHolderVerificationMethod = getValue(0x9F34)
            terminalType = getValue(0x9F35)
            transactionAmount = getValue(0x9F02)
            amountOther = getValue(0x9F03)
            cardSequenceNumber = getValue(0x5f34)
            aid = getValue(0x9F06)

            cardMethod = CardReaderEvent.CHIP
            transactionCurrency = getValue(0x5F2A)
            terminalCountryCode = getValue(0x9F1A)
            terminalCapabilities = getValue(0x9F33)
        }

        magStripData?.run {
            track1 = this[0]!!
            track2 = this[1]!!
            populateFromTrack1(track1)

            cardMethod = CardReaderEvent.MAG_STRIPE
        }
    }

    private fun populateFromTrack1(track1: String) {
//        var separator = if (track2.indexOf("^") < 0) "=" else "^"
        val fieldArray = track1.split('^', '=')

        pan = fieldArray[0].substring(1)
        holder = fieldArray[1]
        exp = fieldArray[2].substring(0, 4)
        src = fieldArray[2].substring(4, 7)
    }


    private fun populateSRC(track2: String) {
        var markerIndex = track2.indexOf("D")
        if (markerIndex < 0) {
            markerIndex = track2.indexOf("=")
        }
        src = track2.substring(markerIndex + 5, markerIndex + 8)
    }

    private fun getValue(tag: Int, hex: Boolean = false): String {
        val tlv = EmvTLV(tag)

        return when (emvService?.Emv_GetTLV(tlv)) {
            EmvService.EMV_TRUE -> when {
                hex -> String(tlv.Value)
                else -> tlv.Value.hexString
            }
            else -> ""
        }
    }

    private fun getValue(tag: Int, hex: Boolean, fpadded: Boolean): String {
        val value = StringBuffer(this.getValue(tag, hex))
        if (fpadded) {
            if (value[value.toString().length - 1] == 'F') {
                value.deleteCharAt(value.toString().length - 1)
            }
            value.toString()
        }
        return value.toString()
    }

    private fun appendMeta(pattern: String, value: String): String {
        return String.format("%s%02x%s", pattern, value.length / 2, value)
    }

    companion object {
        fun create(emvService: EmvService): TelpoEmvCardData {
            return TelpoEmvCardData(emvService)
        }

        fun create(magStripData: Array<String?>): TelpoEmvCardData {
            return TelpoEmvCardData(magStripData = magStripData)
        }
    }
}
