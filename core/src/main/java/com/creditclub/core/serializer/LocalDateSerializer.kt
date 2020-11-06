package com.creditclub.core.serializer

import com.creditclub.core.util.toLocalDate
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer {

    override val descriptor = PrimitiveSerialDescriptor("LocalDateSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return decoder.decodeString().toLocalDate()
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}