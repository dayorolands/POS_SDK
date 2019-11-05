package com.creditclub.core.serializer

import com.creditclub.core.type.TransactionType
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntDescriptor

@Serializer(forClass = TransactionType::class)
object TransactionTypeSerializer {

    override val descriptor: IntDescriptor
        get() = IntDescriptor

    override fun deserialize(decoder: Decoder): TransactionType {
        return TransactionType.find(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, obj: TransactionType) {
        encoder.encodeInt(obj.code)
    }
}