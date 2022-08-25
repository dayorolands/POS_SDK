package com.cluster.core.type

enum class FeatureCodes(transactionType: TransactionType) {
    BPM(TransactionType.BillsPayment),
    CWT(TransactionType.POSCashOut),
    LFT(TransactionType.LocalFundsTransfer),
    IFT(TransactionType.FundsTransferCommercialBank),
    DPS(TransactionType.CashIn),
    TWT(TransactionType.CashOut),
    COL(TransactionType.CollectionPayment),
    ATP(TransactionType.Recharge),
    BEQ(TransactionType.BalanceEnquiry);

    companion object {
        fun find(index: String): FeatureCodes {
            return values().find { it.equals(index) }
                ?: throw IndexOutOfBoundsException("No feature code exists with transaction type: $index")
        }
    }
}