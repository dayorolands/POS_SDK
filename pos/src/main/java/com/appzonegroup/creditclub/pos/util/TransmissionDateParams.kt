package com.appzonegroup.creditclub.pos.util

import com.cluster.core.util.format
import java.time.Instant

@JvmInline
value class TransmissionDateParams(val date: Instant = Instant.now()) {
    val localDate get() = date.format("MMdd")
    val localTime get() = date.format("HHmmss")
    val transmissionDateTime get() = date.format("MMddHHmmss")
}