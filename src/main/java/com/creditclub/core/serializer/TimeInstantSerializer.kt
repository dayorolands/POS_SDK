package com.creditclub.core.serializer

import com.creditclub.core.util.toInstant
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import org.threeten.bp.Instant

@Serializer(forClass = Instant::class)
object TimeInstantSerializer {

    override val descriptor: StringDescriptor
        get() = StringDescriptor

    override fun deserialize(decoder: Decoder): Instant {
        return decoder.decodeString().toInstant()
    }

    override fun serialize(encoder: Encoder, obj: Instant) {
        encoder.encodeString(obj.toString())
    }
}