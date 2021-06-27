package com.telpo.emv.util

import java.security.MessageDigest
import java.util.*

internal inline val ByteArray.hexString: String
    get() {
        val stringBuilder = StringBuilder("")
        if (size <= 0) return ""
        val buffer = CharArray(2)
        for (i in indices) {
            buffer[0] = Character.forDigit(get(i).toInt() ushr 4 and 0x0F, 16)
            buffer[1] = Character.forDigit(get(i).toInt() and 0x0F, 16)
            stringBuilder.append(buffer)
        }
        return stringBuilder.toString().uppercase(Locale.getDefault())
    }

internal inline val String.hexBytes: ByteArray
    get() {
        if (isBlank()) return byteArrayOf()
        val len = length / 2
        val result = ByteArray(len)
        val achar = uppercase(Locale.getDefault()).toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = (achar[pos].index shl 4 or achar[pos + 1].index).toByte()
        }
        return result
    }

internal inline val String.hexByte: Byte
    get() {
        if (length == 0) return 0
        if (length == 1) return uppercase(Locale.getDefault())[0].index.toByte()
        val achar = uppercase(Locale.getDefault()).toCharArray()
        return (achar[0].index shl 4 or achar[1].index).toByte()
    }

internal inline val Char.index: Int get() = "0123456789ABCDEF".indexOf(this)

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

internal inline val ByteArray.sha256String: String
    get() {
        val sb = StringBuffer()
        val md = MessageDigest.getInstance("SHA-256")
        md.update(this)
        val byteData = md.digest()
        for (i in byteData.indices) {
            sb.append(
                ((byteData[i].toInt() and 0xff) + 0x100).toString(16).substring(1)
            )
        }
        return sb.toString()
    }