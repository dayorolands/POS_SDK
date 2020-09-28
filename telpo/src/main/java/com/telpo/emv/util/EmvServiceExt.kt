package com.telpo.emv.util

import com.telpo.emv.EmvService
import com.telpo.emv.EmvTLV

fun EmvService.getValue(tag: Int, hex: Boolean, padded: Boolean): String {
    val value = StringBuffer(getValue(tag, hex))
    if (padded) {
        if (value[value.toString().length - 1] == 'F') {
            value.deleteCharAt(value.toString().length - 1)
        }
        value.toString()
    }
    return value.toString()
}

fun EmvService.getValue(tag: Int, hex: Boolean = false): String {
    val tlv = EmvTLV(tag)

    return when (Emv_GetTLV(tlv)) {
        EmvService.EMV_TRUE -> when {
            hex -> String(tlv.Value)
            else -> StringUtil.bytesToHexString(tlv.Value)
        }
        else -> ""
    }
}