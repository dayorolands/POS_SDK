package com.creditclub.core.serializer

import com.creditclub.core.util.toLocalDate
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import org.threeten.bp.LocalDate

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer {

    override val descriptor: StringDescriptor
        get() = StringDescriptor

    override fun deserialize(decoder: Decoder): LocalDate {
        return decoder.decodeString().toLocalDate()
    }

    override fun serialize(encoder: Encoder, obj: LocalDate) {
        encoder.encodeString(obj.toString())
    }
}