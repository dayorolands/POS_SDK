package com.creditclub.core.util

import com.creditclub.core.BuildConfig
import java.security.SecureRandom


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/10/2019.
 * Appzone Ltd
 */

fun <T> debugValue(value: T): T? {
    if (BuildConfig.DEBUG) return value

    return null
}

fun Double.toNairaString(): String {
    return "NGN$this"
}

fun generateRRN(): String {
    val stan = SecureRandom().nextInt(1000000)
    val rrnPart = SecureRandom().nextInt(100000)
    val stanString = String.format("%06d", stan)

    return String.format("1%05d", rrnPart) + stanString
}