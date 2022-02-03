package com.cluster.core.serializer

import com.cluster.core.type.TransactionType
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = TransactionType::class)
object TransactionTypeSerializer {

    override val descriptor = PrimitiveSerialDescriptor("TransactionTypeSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): TransactionType {
        return TransactionType.find(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: TransactionType) {
        encoder.encodeInt(value.code)
    }
}