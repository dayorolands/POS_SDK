package com.creditclub.pos

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