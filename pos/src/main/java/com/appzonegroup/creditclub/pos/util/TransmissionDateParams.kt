package com.appzonegroup.creditclub.pos.util

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class TransmissionDateParams(val date: Instant = Instant.now()) {
    val localDate: String
    val localTime: String
    val transmissionDateTime: String

    init {
        val localDateDf = DateTimeFormatter.ofPattern("MMdd").withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of(AppConstants.ZONE_ID))
        val localTimeDf = DateTimeFormatter.ofPattern("HHmmss").withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of(AppConstants.ZONE_ID))
        val dateTimeDf = DateTimeFormatter.ofPattern("MMddHHmmss").withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of(AppConstants.ZONE_ID))

        this.localDate = localDateDf.format(date)
        this.localTime = localTimeDf.format(date)
        this.transmissionDateTime = dateTimeDf.format(date)
    }
}