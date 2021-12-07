@file:JvmName("XmlToJson")

package com.cluster.utility.online

import org.json.JSONObject
import fr.arnaudguyon.xmltojsonlib.XmlToJson

fun convertXmlToJson(xml: String?): JSONObject? {
    val xmlToJson: XmlToJson = XmlToJson.Builder(xml ?: "").build()
    return xmlToJson.toJson()
}