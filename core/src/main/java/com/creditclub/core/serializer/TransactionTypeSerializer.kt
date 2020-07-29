package com.creditclub.core.serializer

import com.creditclub.core.type.TransactionType
import kotlinx.serialization.*

@Serializer(forClass = TransactionType::class)
object TransactionTypeSerializer {

    override val descriptor = PrimitiveDescriptor("TransactionTypeSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): TransactionType {
        return TransactionType.find(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: TransactionType) {
        encoder.encodeInt(value.code)
    }
}