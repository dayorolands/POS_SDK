@file:JvmName("XmlToJson")

package com.appzonegroup.app.fasttrack.utility.online

import org.json.JSONObject
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import org.xmlpull.v1.XmlPullParser

fun convertXmlToJson(xml: String?): JSONObject? {
    val xmlToJson: XmlToJson = XmlToJson.Builder(xml ?: "").build()
    return xmlToJson.toJson()
}