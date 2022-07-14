package com.cluster

object Routes {
    const val Home = "home"
    const val FundsTransfer = "transactions/funds-transfer"
    const val CardlessWithdrawal = "transactions/cardless-withdrawal"
    const val TransactionSummary = "transactions/summary"
    const val PinChange = "agent/pin-change"
    const val ChangePassword = "auth/password/change"
    const val Receipt = "transactions/receipt"
    const val SupportCases = "support/cases"
    const val SupportConversation = "support/cases/{reference}/{title}/thread"
    const val LogCase = "support/cases/new"
    const val UssdWithdrawal = "transactions/ussd/new"
    const val PendingTransactions = "transactions/ussd/new"
    const val Subscription = "subscription"
    const val NewSubscription = "subscription/new"
    const val UpgradeSubscription = "subscription/upgrade"
    const val ChangeSubscription = "subscription/change"
    const val ExtendSubscription = "subscription/extend"
    const val SubscriptionHistory = "subscription/history"
    const val AgentLoanRequest = "agent/loan/request"
    const val AgentLoanHistory = "agent/loan/history"

    fun supportConversation(reference: String, title: String) =
        "support/cases/${reference}/${title}/thread"
}