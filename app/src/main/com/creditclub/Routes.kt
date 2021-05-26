package com.creditclub

object Routes {
    const val Home = "home"
    const val FundsTransfer = "transactions/funds-transfer"
    const val PinChange = "agent/pin-change"
    const val Receipt = "transactions/receipt"
    const val SupportCases = "support/cases"
    const val SupportConversation = "support/cases/{reference}/{title}/thread"
    const val LogCase = "support/cases/new"

    val supportConversation = { reference: String, title: String ->
        "support/cases/${reference}/${title}/thread"
    }
}