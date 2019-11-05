package com.appzonegroup.creditclub.pos.card

enum class AccountType(val code: String) {
    Default("00"),
    Savings("10"),
    Current("20"),
    Credit("30"),
    Universal("40"),
    Investment("50")
}