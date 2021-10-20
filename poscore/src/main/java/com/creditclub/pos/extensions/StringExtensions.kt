package com.creditclub.pos.extensions

import java.util.*

fun stringToHexBytes(dataString: String): ByteArray {
    if (dataString.isBlank()) return byteArrayOf()
    var value = dataString
    if (value.length % 2 != 0) {
        value = "0${value}"
    }
    val len = value.length / 2
    val result = ByteArray(len)
    val chars = value.uppercase(Locale.getDefault()).toCharArray()
    for (i in 0 until len) {
        val pos = i * 2
        result[i] = (chars[pos].index shl 4 or chars[pos + 1].index).toByte()
    }
    return result
}

inline val String.hexBytes: ByteArray get() = stringToHexBytes(this)

inline val String.hexByte: Byte
    get() {
        if (length == 0) return 0
        if (length == 1) return uppercase(Locale.getDefault())[0].index.toByte()
        val achar = uppercase(Locale.getDefault()).toCharArray()
        return (achar[0].index shl 4 or achar[1].index).toByte()
    }

inline val Char.index: Int get() = "0123456789ABCDEF".indexOf(this)

inline val ByteArray.hexString: String get() = bytesToHexString(this)

fun bytesToHexString(dataBytes: ByteArray): String {
    val stringBuilder = StringBuilder("")
    if (dataBytes.isEmpty()) return ""
    val buffer = CharArray(2)
    for (i in dataBytes.indices) {
        buffer[0] = Character.forDigit(dataBytes[i].toInt() ushr 4 and 0x0F, 16)
        buffer[1] = Character.forDigit(dataBytes[i].toInt() and 0x0F, 16)
        stringBuilder.append(buffer)
    }
    return stringBuilder.toString().uppercase(Locale.getDefault())
}
