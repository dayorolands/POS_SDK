package com.cluster.core.type

enum class TokenType {
    AccountOpening,
    Withdrawal,
    Deposit,
    Transfer,
    BalanceEnquiry,
    NameEnquiry,
    PayBill,
    BVNUpdate,
    PinChange;

    val label: String get() = toString()
}