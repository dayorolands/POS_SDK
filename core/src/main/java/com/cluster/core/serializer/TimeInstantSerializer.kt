package com.cluster.core.serializer

import com.cluster.core.util.CREDIT_CLUB_DATE_PATTERN
import com.cluster.core.util.CREDIT_CLUB_REQUEST_DATE_PATTERN
import com.cluster.core.util.format
import com.cluster.core.util.instantFromPattern
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializer(forClass = Instant::class)
object TimeInstantSerializer {

    override val descriptor = PrimitiveSerialDescriptor("TimeInstantSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return decoder.decodeString().instantFromPattern(CREDIT_CLUB_DATE_PATTERN)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.format(CREDIT_CLUB_REQUEST_DATE_PATTERN))
    }
}