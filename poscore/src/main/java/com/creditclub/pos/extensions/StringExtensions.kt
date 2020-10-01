package com.creditclub.pos.extensions

import java.util.*

inline val String.hexBytes: ByteArray
    get() {
        if (isBlank()) return byteArrayOf()
        val len = length / 2
        val result = ByteArray(len)
        val achar = toUpperCase(Locale.getDefault()).toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = (achar[pos].index shl 4 or achar[pos + 1].index).toByte()
        }
        return result
    }

inline val String.hexByte: Byte
    get() {
        if (length == 0) return 0
        if (length == 1) return toUpperCase(Locale.getDefault())[0].index.toByte()
        val achar = toUpperCase(Locale.getDefault()).toCharArray()
        return (achar[0].index shl 4 or achar[1].index).toByte()
    }

inline val Char.index: Int get() = "0123456789ABCDEF".indexOf(this)

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
