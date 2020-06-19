package com.creditclub.core.serializer

import com.creditclub.core.util.toLocalDate
import kotlinx.serialization.*
import org.threeten.bp.LocalDate

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer {

    override val descriptor = PrimitiveDescriptor("LocalDateSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return decoder.decodeString().toLocalDate()
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}