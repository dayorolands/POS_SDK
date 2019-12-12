package com.creditclub.core.config

import com.creditclub.core.type.TransactionType

interface IInstitutionConfig {

    var hasOnlineFunctions: Boolean

    var hasHlaTagging: Boolean

    var transactionTypes: List<TransactionType>

    var flows: FlowConfig

    var categories: CategoryConfig
}
