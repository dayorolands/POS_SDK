package com.dspread.qpos.utils

import android.util.Xml
import com.cluster.pos.PosParameter
import com.cluster.pos.extensions.*
import org.json.JSONObject
import org.xmlpull.v1.XmlSerializer

private fun emvConfig(posParameter: PosParameter): String {
    val xmlSerializer = Xml.newSerializer()
    val managementData = posParameter.managementData
    val emvAidList = posParameter.emvAidList!!
    val capkList = posParameter.capkList!!
    val xmlString = xmlSerializer.document {
        var len = emvAidList.length()
        for (i in 0 until len) {
            val obj = emvAidList[i] as JSONObject
            appElement(obj, managementData)
        }

        len = capkList.length()
        for (i in 0 until len) {
            val obj = capkList[i] as JSONObject
            capkElement(obj)
        }
    }
    return xmlString
}

private fun XmlSerializer.capkElement(obj: JSONObject) {
    element("capk") {
        element("9F06", (obj.optString("35") ?: ""))
        element("9F22", (obj.optString("32") ?: ""))
        element("DF05", "20311222")
        element("DF04", (obj.optString("38") ?: ""))
        element("DF02", (obj.optString("37")?.replace("\n", "") ?: ""))
        element("DF06", (obj.optString("36") ?: ""))
        element("DF07", (obj.optString("40") ?: ""))
        element("DF03", (obj.optString("39") ?: ""))
    }
}

private fun XmlSerializer.appElement(obj: JSONObject, managementData: PosParameter.ManagementData) {
    element("app") {
        element("9F06", obj.aid15)
        element("5F2A", "0566")
        element("5F36", "00")
        element("9F01", "000000636092") // Acquirer Identifier
        element("9F09", stringWithFallback(obj.appVersion18, "0100"))
        element(
            "9F15",
            stringWithFallback(managementData.merchantCategoryCode, "0000")
        ) // Merchant Category Code
        element(
            "9F16",
            managementData.cardAcceptorId.toByteArray().hexString
        ) // Merchant Identifier，add 0x00 if the digits is not enough
        element("9F1A", "0566")
        element("9F1B", stringWithFallback(obj.tflDomestic22, "00000000"))
        element("9F1C", "2076DK34".toByteArray().hexString) // Terminal Identification
        element("9F1E", "3833323031494343") // Interface Device (IFD) Serial Number
        element("9F33", "E040C8") // Terminal Capabilities
        element("9F35", "22") // Terminal Type
        element("9F39", "05") // Point-of-Service (POS) Entry Mode
        element("9F3C", "0566") // Transaction Reference Currency Code
        element("9F3D", "02") // Application Reference Currency
        element("9F40", "7000B0A001") // Additional Terminal Capabilities
        element(
            "9F4E",
            managementData.cardAcceptorLocation.toByteArray().hexString
        ) // Merchant Name and Location，add 0x00 if the digits is not enough
        element("9F66", "32C04000") // Terminal Default Transaction Qualifiers
        element("9F73", "000000") // Currency Conversion Factor
        element("9F7B", "000000001388") // Electronic Cash Terminal Transaction Limit
        element(
            "DF01",
            stringWithFallback(obj.selectionPriority19.fixedHex, "01")
        ) // Application Selection Indicator
        element("DF11", obj.defaultTacValue29)
        element("DF12", obj.tacOnline31)
        element("DF13", obj.tacDenial30)
        element("DF14", obj.ddol20)
        element("DF15", stringWithFallback(obj.offlineThresholdDomestic24, "00000000"))
        element("DF16", stringWithFallback(obj.maxTargetDomestic25, "63"))
        element("DF17", "00")
        element("DF19", "000000001388") // Terminal Contactless Offline Floor Limit
        element("DF20", "999999999999") // Terminal Contactless Transaction Limit
        element("DF21", "000000000000") // Terminal Execute Cvm Limit
        element("DF60", "00") // ICS
        element("DF72", "F4F0F0FAAFFE8000") // ICS
        element("DF73", "01") // Status
        element("DF74", "0F") // Identity Of Each Limit Exist
        element("DF75", "01") // Terminal Status Check，Default value = 0
        element("DF76", stringWithFallback(obj.tdol21, "9F1A0295059A039C01")) // Default Tdol
        element("DF78", "000000000000") // Contactless CVM Required limit
        element("DF79", "E040C8") // Contactless Terminal Capabilities
        element("DF7A", "7000B0A001") // Contactless Additional Terminal Capabilities
        element("DF7B", obj.defaultTacValue29)
        element("DF7C", obj.tacOnline31)
        element("DF7D", obj.tacDenial30)
    }
}

private fun stringWithFallback(value: String?, defaultValue: String): String {
    return when {
        value.isNullOrBlank() -> defaultValue
        else -> value
    }
}