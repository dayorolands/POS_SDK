package com.cluster.pos.card

enum class TransactionType(val type: String) {
    Purchase("PURCHASE"),
    Unknown("UNKNOWN"),
    CashAdvance("CASH ADVANCE"),
    Refund("REFUND"),
    CashBack("CASH BACK"),
    Reversal("REVERSAL"),
    Balance("BALANCE INQUIRY"),
    SalesComplete("SALES COMPLETION"),
    PreAuth("PRE AUTHORIZATION")
}

val cardPaymentAIDConverter = hashMapOf(
    "A0000000032020" to "VISA",
    "A0000000032010" to "VISA Debit",
    "A0000004540010" to "Etranzact Genesis Card",
    "A0000004540011" to "Etranzact Genesis Card",
    "A0000000042203" to "MasterCard US",
    "A0000000041010" to "MasterCard",
    "A0000000031010" to "VISA Debit",
    "A0000000044010" to "MasterCard",
    "A0000000041030" to "MasterCard",
    "A0000000046000" to "MasterCard Specific",
    "A0000000043060" to "MasterCard Specific",
    "A0000000042010" to "MasterCard Specific",
    "A0000000043010" to "MasterCard Specific",
    "A0000000045010" to "MasterCard Specific",
    "A0000003710001" to "InterSwitch Verve Card"
)