package com.creditclub.core.util

import java.text.NumberFormat
import java.util.*

fun Number.toCurrencyFormat(
    locale: Locale = Locale("ng", "NG"),
    currencyCode: String = "NGN"
): String {
    val numberFormatter = NumberFormat.getCurrencyInstance(locale)
    numberFormatter.currency = Currency.getInstance(currencyCode)
    return numberFormatter.format(this)
}

fun Number.format(locale: Locale = Locale("ng", "NG")): String {
    val numberFormatter = NumberFormat.getNumberInstance(locale)
    return numberFormatter.format(this)
}