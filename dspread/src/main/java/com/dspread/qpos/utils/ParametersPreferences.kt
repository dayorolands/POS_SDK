package com.dspread.qpos.utils

import android.util.Log
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.PosParameter
import com.creditclub.pos.extensions.*
import org.json.JSONObject

private inline val String.fixedHex: String?
    get() = if (length % 2 == 1) "0$this"
    else this

val PosParameter.capKeys: List<String>
    get() {
        val capkArray = capkList ?: return emptyList()
        val newCapkList = mutableListOf<String>()
        try {
            val len = capkArray.length()
            for (i in 0 until len) {
                debugOnly { Log.d("Params", "") }
                val obj = capkArray[i] as JSONObject
                val curr = mapCAPK(obj)
                if (!newCapkList.contains(curr)) newCapkList.add(curr)
            }
        } catch (e: Exception) {
            debugOnly { Log.e("Params", e.message) }
        }
        return newCapkList
    }

val PosParameter.emvAids: ArrayList<String>
    get() {
        val newAidList = ArrayList<String>()
        val aidArray = emvAidList ?: return newAidList
        try {
            val len = aidArray.length()
            for (i in 0 until len) {
                debugOnly { Log.d("Params", "") }
                val obj = aidArray[i] as JSONObject
                val curr = mapAID(obj)
                if (!newAidList.contains(curr)) newAidList.add(curr)
            }
        } catch (e: Exception) {
            debugOnly { Log.e("Params", e.message) }
        }
        return newAidList
    }

val PosParameter.parameters: String
    get() {
        var parameter = ""
        try {
            val obj = JSONObject(managementDataString)
            parameter = mapParameter(obj)
        } catch (e: Exception) {
            debugOnly { Log.e("Params", e.message) }
        }
        return parameter
    }

private fun PosParameter.mapAID(obj: JSONObject): String {
    val managementData = managementData
    return "".appendTlv("9F06", obj.aid15)
        .appendTlv("5F2A", decToHexString("0566"))
        .appendTlv("5F36", decToHexString("00"))
        .appendTlv("9F01", null, "000000636092") // Acquirer Identifier
        .appendTlv("9F09", obj.appVersion18, "0100")
        .appendTlv("9F15", managementData.merchantCategoryCode, "0000") // Merchant Category Code
        .appendTlv(
            "9F16",
            null,
            managementData.cardAcceptorId.toByteArray().hexString
        ) // Merchant Identifier，add 0x00 if the digits is not enough
        .appendTlv("9F1A", decToHexString("0566"))
        .appendTlv("9F1B", obj.tflDomestic22, "00000000")
        .appendTlv("9F1C", null, "2076DK34".toByteArray().hexString) // Terminal Identification
        .appendTlv("9F1E", null, "3833323031494343") // Interface Device (IFD) Serial Number
        .appendTlv("9F33", null, "E040C8") // Terminal Capabilities
        .appendTlv("9F35", null, "22") // Terminal Type
        .appendTlv("9F39", null, "05") // Point-of-Service (POS) Entry Mode
        .appendTlv("9F3C", null, "0566") // Transaction Reference Currency Code
        .appendTlv("9F3D", null, "02") // Application Reference Currency
        .appendTlv("9F40", null, "7000B0A001") // Additional Terminal Capabilities
        .appendTlv(
            "9F4E",
            null,
            managementData.cardAcceptorLocation.toByteArray().hexString
        ) // Merchant Name and Location，add 0x00 if the digits is not enough
        .appendTlv("9F66", null, "32C04000") // Terminal Default Transaction Qualifiers
        //.appendTlv("9F73", null, "000000") // Currency Conversion Factor
        .appendTlv("9F7B", null, "000000001388") // Electronic Cash Terminal Transaction Limit
        .appendTlv("DF01", obj.selectionPriority19.fixedHex, "01") // Application Selection Indicator
        .appendTlv("DF11", obj.defaultTacValue29)
        .appendTlv("DF12", obj.tacOnline31)
        .appendTlv("DF13", obj.tacDenial30)
        .appendTlv("DF14", obj.ddol20)
        .appendTlv("DF15", obj.offlineThresholdDomestic24, "00000000")
        .appendTlv("DF16", obj.maxTargetDomestic25, "63")
        .appendTlv("DF17", obj.targetPercentageDomestic27)
        .appendTlv("DF19", null, "000000001388") // Terminal Contactless Offline Floor Limit
        .appendTlv("DF20", null, "999999999999") // Terminal Contactless Transaction Limit
        .appendTlv("DF21", null, "000000000000") // Terminal Execute Cvm Limit
        .appendTlv("DF72", null, "F4F0F0FAAFFE8000") // ICS
        .appendTlv("DF73", null, "01") // Status
        .appendTlv("DF74", null, "0F") // Identity Of Each Limit Exist
        .appendTlv("DF75", null, "01") // Terminal Status Check，Default value = 0
        .appendTlv("DF76", obj.tdol21, "") // Default Tdol
        .appendTlv("DF78", null, "000000000000") // Contactless CVM Required limit
        .appendTlv("DF79", null, "E040C8") // Contactless Terminal Capabilities
        .appendTlv("DF7A", null, "F000F0A001") // Contactless Additional Terminal Capabilities
        .appendTlv("DF7B", null, "")
}

private fun mapCAPK(obj: JSONObject): String {
    return "9F06" +
            mapToTLV(obj.optString("35") ?: "") +
            "9F22" +
            mapToTLV(obj.optString("32") ?: "") +
            "DF05" +  // NO VALUE - KEY
            decToHexString("0") +  // NO VALUE - VALUE
            "DF04" +
            mapToTLV(obj.optString("38") ?: "") +
            "DF02" +
            mapToTLV(obj.optString("37") ?: "") +
            "DF06" +
            mapToTLV(obj.optString("36") ?: "") +
            "DF07" +
            mapToTLV(obj.optString("40") ?: "") +
            "DF03" +
            mapToTLV(obj.optString("39") ?: "")
}

fun String.appendTlv(tag: String, value: String?, defaultValue: String? = null): String {
    val newValue =
        if (value != null && value.isBlank() && defaultValue != null) defaultValue
        else value ?: defaultValue ?: return this

    return this + tag + mapToTLV(newValue)
}

private fun mapParameter(obj: JSONObject): String {
    return try {
        "9F06" +
                mapToTLV(obj.optString("03") ?: "") +
                "9F1A" +
                mapToTLV(obj.optString("05") ?: "") +
                "5F2A" +
                mapToTLV(obj.optString("06") ?: "") +
                "DF03" +
                mapToTLV(obj.optString("08") ?: "")
    } catch (e: Exception) {
        debugOnly { Log.e("Params", e.message) }
        ""
    }
}

private fun decToHexString(number: String): String {
    return number // HexDump.toHexString(Integer.parseInt(number));
}

private fun mapToTLV(value: String): String {
    var newValue = value
    if (newValue.length % 2 == 1) newValue = "0$newValue"
    val hex: ByteArray = newValue.hexBytes
    return hexByteArrayLength(hex) + newValue
}

private fun hexByteArrayLength(value: ByteArray): String {
    val longHex: String = value.size.hexBytes.hexString
    val len = longHex.length
    return when {
        len == 1 -> "0$longHex"
        len == 2 -> longHex
        len > 2 -> longHex.substring(len - 2, len)
        else -> "00"
    }
}

private inline val Int.hexBytes: ByteArray
    get() {
        val array = ByteArray(4)
        val i = this
        array[3] = (i and 0xFF).toByte()
        array[2] = (i shr 8 and 0xFF).toByte()
        array[1] = (i shr 16 and 0xFF).toByte()
        array[0] = (i shr 24 and 0xFF).toByte()

        return array
    }