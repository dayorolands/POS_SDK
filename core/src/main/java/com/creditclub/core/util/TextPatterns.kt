package com.creditclub.core.util

import java.util.regex.Pattern


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/23/2019.
 * Appzone Ltd
 */
object TextPatterns {
    val invalidAddress = Pattern.compile("[\$&+:;=\\\\\\\\?@#|/'<>^*()%!{}]", Pattern.CASE_INSENSITIVE)
    val specialCharsPattern = Pattern.compile("[\$&+:;=\\\\\\\\?@#|/'<>^*()%!{}.,]", Pattern.CASE_INSENSITIVE)
    val includesNumbers = Pattern.compile("[\$0-9]", Pattern.CASE_INSENSITIVE)
}