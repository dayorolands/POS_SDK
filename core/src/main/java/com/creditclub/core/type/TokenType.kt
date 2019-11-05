package com.creditclub.core.type

enum class TokenType(label: String? = null) {
    AccountOpening,
    Withdrawal,
    Deposit,
    Transfer,
    BalanceEnquiry,
    NameEnquiry,
    PayBill,
    BVNUpdate,
    PinChange;

    var label: String? = label
        get() = field ?: toString()
}