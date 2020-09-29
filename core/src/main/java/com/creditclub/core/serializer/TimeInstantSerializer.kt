package com.creditclub.core.serializer

import com.creditclub.core.util.toInstant
import kotlinx.serialization.*
import java.time.Instant

@Serializer(forClass = Instant::class)
object TimeInstantSerializer {

    override val descriptor = PrimitiveDescriptor("TimeInstantSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return decoder.decodeString().toInstant()
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}