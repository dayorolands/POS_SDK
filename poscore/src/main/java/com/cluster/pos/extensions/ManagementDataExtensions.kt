package com.cluster.pos.extensions

import org.json.JSONObject
import kotlin.reflect.KProperty

@JvmInline
value class ManagementDataDelegate(private val key: String) {
    operator fun getValue(jsonObject: JSONObject, prop: KProperty<*>): String {
        return jsonObject.optString(key)
    }
}

private fun managementDataStore(key: String): ManagementDataDelegate {
    return ManagementDataDelegate(key)
}

val JSONObject.channelSerialNumber01 by managementDataStore("01")
val JSONObject.aidIndex13 by managementDataStore("13")
val JSONObject.airn14 by managementDataStore("14")
val JSONObject.aid15 by managementDataStore("15")
val JSONObject.match16 by managementDataStore("16")
val JSONObject.appName17 by managementDataStore("17")
val JSONObject.appVersion18 by managementDataStore("18")
val JSONObject.selectionPriority19 by managementDataStore("19")
val JSONObject.ddol20 by managementDataStore("20")
val JSONObject.tdol21 by managementDataStore("21")
val JSONObject.tflDomestic22 by managementDataStore("22")
val JSONObject.tflInternational23 by managementDataStore("23")
val JSONObject.offlineThresholdDomestic24 by managementDataStore("24")
val JSONObject.maxTargetDomestic25 by managementDataStore("25")
val JSONObject.maxTargetInternational26 by managementDataStore("26")
val JSONObject.targetPercentageDomestic27 by managementDataStore("27")
val JSONObject.targetPercentageInternational28 by managementDataStore("28")
val JSONObject.defaultTacValue29 by managementDataStore("29")
val JSONObject.tacDenial30 by managementDataStore("30")
val JSONObject.tacOnline31 by managementDataStore("31")

val JSONObject.keyIndex32 by managementDataStore("32")
val JSONObject.internalReferenceNumber33 by managementDataStore("33")
val JSONObject.keyName34 by managementDataStore("34")
val JSONObject.rid35 by managementDataStore("35")
val JSONObject.hashAlgorithm36 by managementDataStore("36")
val JSONObject.modulus37 by managementDataStore("37")
val JSONObject.exponent38 by managementDataStore("38")
val JSONObject.hash39 by managementDataStore("39")
val JSONObject.keyAlgorithm40 by managementDataStore("40")