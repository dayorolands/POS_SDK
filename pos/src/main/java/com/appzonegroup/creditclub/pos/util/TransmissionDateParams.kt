package com.appzonegroup.creditclub.pos.util

import com.creditclub.core.util.format
import java.time.Instant

class TransmissionDateParams(val date: Instant = Instant.now()) {
    val localDate by lazy { date.format("MMdd") }
    val localTime by lazy { date.format("HHmmss") }
    val transmissionDateTime by lazy { date.format("MMddHHmmss") }
}