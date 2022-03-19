package com.cluster.core.util

import android.util.Patterns
import java.util.regex.Pattern


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 7/23/2019.
 * Appzone Ltd
 */

val invalidAddressPattern =
    Pattern.compile("[\$&+:;=\\\\?@#|/'<>^*()%!{}]", Pattern.CASE_INSENSITIVE)
val specialCharsPattern =
    Pattern.compile("[\$&+:;=\\\\?@#|/'<>^*()%!{},]", Pattern.CASE_INSENSITIVE)

fun String.includesSpecialCharacters(pattern: Pattern = specialCharsPattern): Boolean {
    return isNotEmpty() && pattern.matcher(this).matches()
}

fun String.isValidEmail(): Boolean =
    this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.includesNumbers(): Boolean =
    this.isNotEmpty() && TextPatterns.includesNumbers.matcher(this).find()

fun String?.mask(first: Int, last: Int): String {
    if (this == null) return ""

    if (length <= first + last) return this

    return "${substring(0, first)}${"*".repeat(length - first - last)}${
        substring(
            length - last,
            length
        )
    }"
}