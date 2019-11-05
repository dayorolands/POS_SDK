package com.creditclub.core.config

class FlowConfig {
    var accountOpening: AccountOpeningConfig = AccountOpeningConfig()
    var walletOpening: AccountOpeningConfig? = AccountOpeningConfig()
    var tokenWithdrawal: TokenWithdrawalConfig = TokenWithdrawalConfig()
    var customerPinChange: Any? = Any()
    var bvnUpdate: Any? = Any()
}
