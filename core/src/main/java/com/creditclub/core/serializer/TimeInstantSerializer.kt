package com.creditclub.core.serializer

import com.creditclub.core.util.CREDIT_CLUB_DATE_PATTERN
import com.creditclub.core.util.format
import com.creditclub.core.util.instantFromPattern
import kotlinx.serialization.*
import java.time.Instant

@Serializer(forClass = Instant::class)
object TimeInstantSerializer {

    override val descriptor = PrimitiveDescriptor("TimeInstantSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return decoder.decodeString().instantFromPattern(CREDIT_CLUB_DATE_PATTERN)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.format(CREDIT_CLUB_DATE_PATTERN))
    }
}