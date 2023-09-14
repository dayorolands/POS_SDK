package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class MiniStatementTransaction {

    @SerialName("TransactionDate")
    var transactionDate: String? = null

    @SerialName("ReferenceID")
    var referenceID: String? = null

    @SerialName("Narration")
    var narration: String? = null

    @SerialName("Debit")
    var debit: Double? = null

    @SerialName("Credit")
    var credit: Double? = null
}