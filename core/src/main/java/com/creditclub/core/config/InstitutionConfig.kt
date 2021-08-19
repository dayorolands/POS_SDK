package com.creditclub.core.config

import com.creditclub.core.type.TransactionType

interface InstitutionConfig {
    val name: String
    val hasOnlineFunctions: Boolean
    val hasHlaTagging: Boolean
    val transactionTypes: List<TransactionType>
    val flows: FlowConfig
    val categories: CategoryConfig
    val bankAccountNumberLength: Int
}

data class FlowConfig(
    var accountOpening: AccountOpeningConfig? = null,
    var walletOpening: AccountOpeningConfig? = null,
    var tokenWithdrawal: TokenWithdrawalConfig? = null,
    var ussdWithdrawal: Any? = null,
    var customerPinChange: Any? = null,
    var customerBalance: Any? = null,
    var airtime: Any? = null,
    var bvnUpdate: Any? = null,
    var collectionPayment: Any? = null,
    var billPayment: Any? = null,
)

data class TokenWithdrawalConfig(
    val customerPin: Boolean = false,
    val externalToken: Boolean = false,
)

data class CategoryConfig(
    val loans: Boolean = false,
    val customers: Boolean = true,
)

data class AccountOpeningConfig(
    val middleName: Boolean = false,
    val products: Boolean = false,
)
