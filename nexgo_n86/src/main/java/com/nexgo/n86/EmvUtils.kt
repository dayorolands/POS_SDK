package com.nexgo.n86

import android.content.Context
import android.util.Log
import com.cluster.pos.extensions.hexBytes
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.nexgo.oaf.apiv3.emv.*
import java.io.IOException
import java.util.*
import kotlin.jvm.Throws

@Throws(IOException::class)
fun getDefaultCapkList(context: Context): List<CapkEntity> {
    val capkEntityList: MutableList<CapkEntity> = ArrayList()
    val gson = Gson()
    val parser = JsonParser()
    val jsonArray: JsonArray = parser.parse(context.readAssetsTxt("emv_capk.json")).asJsonArray
        ?: return emptyList()
    for (user in jsonArray) {
        val userBean = gson.fromJson(user, CapkEntity::class.java)
        capkEntityList.add(userBean)
    }
    return capkEntityList
}

@Throws(IOException::class)
fun getDefaultAidList(context: Context): List<AidEntity> {
    val aidEntityList: MutableList<AidEntity> = ArrayList()
    val gson = Gson()
    val parser = JsonParser()
    val jsonArray: JsonArray = parser.parse(context.readAssetsTxt("emv_aid.json")).asJsonArray
        ?: return emptyList()
    for (user in jsonArray) {
        val userBean = gson.fromJson(user, AidEntity::class.java)
        val jsonObject = user.asJsonObject
        if (jsonObject != null) {
            if (jsonObject["emvEntryMode"] != null) {
                val emvEntryMode = jsonObject["emvEntryMode"].asInt
                userBean.aidEntryModeEnum = AidEntryModeEnum.values()[emvEntryMode]
                Log.d("nexgo", "emvEntryMode" + userBean.aidEntryModeEnum)
            }
        }
        aidEntityList.add(userBean)
    }
    return aidEntityList
}

@Throws(IOException::class)
private fun Context.readAssetsTxt(fileName: String): String {
    return assets.open(fileName).use { inputStream ->
        val size = inputStream.available()
        // Read the entire asset into a local byte buffer.
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        // Convert the buffer into a string.
        String(buffer, Charsets.UTF_8)
    }
}

inline val Int.hexString get() = toString(16)

fun EmvHandler2.getTag(tagInt: Int): ByteArray {
    return getValue(tagInt).hexBytes
}

fun EmvHandler2.getValue(tag: Int, hex: Boolean = false, fPadded: Boolean = false): String {
    val tagString = tag.hexString
    val rawValue = getTlvByTags(arrayOf(tagString)).substring(tagString.length + 2)

    val value = when {
        hex -> String(rawValue.hexBytes)
        else -> rawValue
    }

    if (fPadded) {
        val stringBuffer = StringBuffer(value)
        if (stringBuffer[stringBuffer.toString().length - 1] == 'F') {
            stringBuffer.deleteCharAt(stringBuffer.toString().length - 1)
        }
        return stringBuffer.toString()
    }

    return value
}

internal infix fun ByteArray.xor(other: ByteArray): ByteArray {
    val result = if (other.size > size) {
        ByteArray(size)
    } else {
        ByteArray(other.size)
    }
    for (i in result.indices) {
        result[i] = (this[i].toInt() xor other[i].toInt()).toByte()
    }
    return result
}