package com.creditclub.core.util

import java.text.NumberFormat
import java.util.*

fun Number.toCurrencyFormat(currencyCode: String = "NGN"): String {
    val currentLocale = Locale("ng", "NG")
    val numberFormatter = NumberFormat.getCurrencyInstance(currentLocale)
    numberFormatter.currency = Currency.getInstance(currencyCode)
    return numberFormatter.format(this)
}