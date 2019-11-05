package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 22/09/2019.
 * Appzone Ltd
 */
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