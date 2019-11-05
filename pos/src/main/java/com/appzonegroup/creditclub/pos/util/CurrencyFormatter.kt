package com.appzonegroup.creditclub.pos.util

import com.appzonegroup.creditclub.pos.BuildConfig
import java.text.NumberFormat
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/20/2019.
 * Appzone Ltd
 */

object CurrencyFormatter {

    private val numberFormatter by lazy {
        val currentLocale = Locale("ng", "NG")
        val numberFormatter = NumberFormat.getCurrencyInstance(currentLocale)

        return@lazy numberFormatter.apply {
            currency = Currency.getInstance("NGN")
        }
    }

    fun format(text: String?): String = try {
        numberFormatter.format((text?.toDouble() ?: 0.00) / 100.0)
    } catch (ex: Exception) {
        if (BuildConfig.DEBUG) ex.printStackTrace()
        "NGN0.00"
    }
}