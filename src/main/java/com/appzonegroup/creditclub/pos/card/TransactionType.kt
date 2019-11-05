package com.appzonegroup.creditclub.pos.card

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