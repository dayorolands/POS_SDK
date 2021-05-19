package com.creditclub.core.config

import com.creditclub.core.type.TransactionType

interface IInstitutionConfig {
    var name: String
    var hasOnlineFunctions: Boolean
    var hasHlaTagging: Boolean
    var transactionTypes: List<TransactionType>
    var flows: FlowConfig
    var categories: CategoryConfig
    var bankAccountNumberLength: Int
}

data class FlowConfig(
    var accountOpening: AccountOpeningConfig? = null,
    var walletOpening: AccountOpeningConfig? = null,
    var tokenWithdrawal: TokenWithdrawalConfig? = null,
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
