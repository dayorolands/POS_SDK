package com.appzonegroup.creditclub.pos.extension

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 7/5/2019.
 * Appzone Ltd
 */

fun Instant.format(pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern).withLocale(Locale.ENGLISH)
        .withZone(ZoneId.of("UTC+01:00")).format(this)
}