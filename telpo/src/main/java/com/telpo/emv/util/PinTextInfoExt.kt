package com.telpo.emv.util

import com.telpo.emv.EmvPinData
import com.telpo.pinpad.PinParam
import com.telpo.pinpad.PinTextInfo
import java.text.NumberFormat
import java.util.*

fun EmvPinData.getPinTextInfo(param: PinParam, cashBackAmount: String? = null): Array<PinTextInfo> {
    val pinTextInfo = arrayListOf(
            PinTextInfo().apply {
                FontColor = 0x0000FF
                FontFile = ""
                FontSize = 48
                PosX = 80
                PosY = 80
                sText = "Enter PIN"
                LanguageID = "en"
            },

            PinTextInfo().apply {
                FontColor = 0x000000
                FontFile = ""
                FontSize = 32
                PosX = 80
                PosY = 140
                sText = "Amount: ${param.Amount}"
                LanguageID = "en"
            }

//            PinTextInfo().apply {
//                FontColor = 0xFF0000
////                FontFile = File(getFilesDir(), "DroidSansHindi.ttf").getAbsolutePath()
//                FontFile = ""
//                FontSize = 48
//                PosX = 280
//                PosY = 140
//                sText = "ताजा ख़बरें"
//                LanguageID = "en"
//            },

//            PinTextInfo().apply {
//                FontColor = 0xFF0000
//                FontFile = ""
//                FontSize = 48
//                PosX = 20
//                PosY = 200
//                sText = "天波"
//                LanguageID = "en"
//            }
    )

    if (!cashBackAmount.isNullOrBlank()) {
        pinTextInfo.add(PinTextInfo().apply {
            FontColor = 0x000000
            FontFile = ""
            FontSize = 32
            PosX = 280
            PosY = 140
            sText = "Cash Back Amount: ${cashBackAmount.toCurrencyFormat()}"
            LanguageID = "en"
        })
    }

    if (IsRetry != 0.toByte()) {
        pinTextInfo.add(PinTextInfo().apply {
            FontColor = 0xFF0000
            FontFile = ""
            FontSize = 32
            PosX = 280
            PosY = 70
            sText = "Tries Left: $RemainCount"
            LanguageID = "en"
        })
    }

    return pinTextInfo.toTypedArray()
}

private fun String?.toCurrencyFormat(): String {
    return try {
        this ?: return "NGN0.00"

        toDouble().toCurrencyFormat()
    } catch (_: Exception) {
        "NGN-XXX"
    }
}

private fun Number.toCurrencyFormat(): String {
    val currentLocale = Locale("ng", "NG")
    val numberFormatter = NumberFormat.getCurrencyInstance(currentLocale)
    numberFormatter.currency = Currency.getInstance("NGN")
    return numberFormatter.format(toDouble() / 100.0)
}