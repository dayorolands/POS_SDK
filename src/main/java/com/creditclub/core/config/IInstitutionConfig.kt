package com.creditclub.core.config

import com.creditclub.core.type.TransactionType

interface IInstitutionConfig {

    var hasOnlineFunctions: Boolean

    var transactionTypes: List<TransactionType>

    var flows: FlowConfig
}
