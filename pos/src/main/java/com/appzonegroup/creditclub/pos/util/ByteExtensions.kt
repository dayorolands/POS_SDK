package com.appzonegroup.creditclub.pos.util

import java.security.MessageDigest
import java.util.*

inline val ByteArray.sha256String: String
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

infix fun ByteArray.xor(other: ByteArray): ByteArray {
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

inline val ByteArray.hexString: String
    get() {
        val stringBuilder = StringBuilder("")
        if (size <= 0) return ""
        val buffer = CharArray(2)
        for (i in indices) {
            buffer[0] = Character.forDigit(get(i).toInt() ushr 4 and 0x0F, 16)
            buffer[1] = Character.forDigit(get(i).toInt() and 0x0F, 16)
            stringBuilder.append(buffer)
        }
        return stringBuilder.toString().toUpperCase(Locale.getDefault())
    }