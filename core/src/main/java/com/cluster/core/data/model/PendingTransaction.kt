package com.cluster.core.data.model

import com.cluster.core.serializer.TimeInstantConverter
import com.cluster.core.serializer.TimeInstantSerializer
import com.cluster.core.serializer.TransactionTypeConverter
import com.cluster.core.type.TransactionType
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity
data class PendingTransaction(
    @Id
    @SerialName("ID")
    var id: Long = 0,

    @Convert(converter = TransactionTypeConverter::class, dbType = Int::class)
    var transactionType: TransactionType,

    var requestJson: String,
    var accountNumber: String,
    var accountName: String,
    var amount: Double,
    var reference: String,

    @Convert(converter = TimeInstantConverter::class, dbType = String::class)
    var createdAt: Instant,

    @Serializable(with = TimeInstantSerializer::class)
    @Convert(converter = TimeInstantConverter::class, dbType = String::class)
    var lastCheckedAt: Instant?,
)