package com.cluster.core.serializer

import com.cluster.core.model.CreditClubImage
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = CreditClubImage::class)
object CreditClubImageSerializer {

    override val descriptor =
        PrimitiveSerialDescriptor("CreditClubImageSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CreditClubImage {
        throw NotImplementedError()
    }

    override fun serialize(encoder: Encoder, value: CreditClubImage) {
        encoder.encodeString("data:image/webp;base64,${value.bitmapString!!}")
    }
}