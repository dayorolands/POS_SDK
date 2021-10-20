package com.creditclub.pos

import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.utils.asDesEdeKey
import com.creditclub.pos.utils.encrypt

fun encryptPinBlock(posParameter: PosParameter, cardNo: String, plainPin: String): ByteArray {
    val pinBlock = "0${plainPin.length}$plainPin".padEnd(16, 'F')
    val panBlock = cardNo.substring(3, cardNo.lastIndex).padStart(16, '0')
    val cryptData = pinBlock.hexBytes xor panBlock.hexBytes
    val secretKey = posParameter.pinKey.hexBytes.asDesEdeKey
    return secretKey.encrypt(cryptData).copyOf(8)
}