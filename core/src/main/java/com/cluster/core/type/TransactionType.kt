package com.cluster.core.type

import androidx.annotation.IntDef
import com.cluster.core.serializer.TransactionTypeSerializer
import kotlinx.serialization.Serializable


@IntDef(value = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,32])
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ReportTypeField

@Serializable(with = TransactionTypeSerializer::class)
enum class TransactionType(val code: Int, val label: String) {
    BalanceEnquiry(1, "Balance Enquiry"),
    MiniStatement(2, "Mini Statement"),
    Recharge(3, "Recharge"),
    BillsPayment(4, "Bills Payment"),
    CashIn(5, "Cash In"),
    CashOut(6, "Cash Out"),
    PINChange(7, "PIN Change"),
    PINReset(8, "PIN Reset"),
    Registration(9, "Registration"),
    CrossBankTokenWithdrawal(39, "CrossBank Token Withdrawal"),
    FundsTransferCommercialBank(10, "Funds Transfer Commercial Bank"),
    FundsTransferCashIn(11, "Funds Transfer Cash In"),
    FundsTransferCashOut(12, "Funds Transfer Cash Out"),
    IntraBankFundTransfer(13, "Intrabank Funds Transfer"),
    AccountActivation(14, "Account Activation"),
    LocalFundsTransfer(15, "Local Funds Transfer"),
    SetDefaultAccount(16, "Set Default Account"),
    LoanRequest(17, "Loan Request"),
    CardLinking(18, "Card Linking"),
    AgentToAgentFundsTransfer(19, "Agent To Agent Funds Transfer"),
    KiaKiaToKiaKia(20, "KiaKia To KiaKia"),
    KiaKiaToSterlingBank(21, "KiaKia To Sterling Bank"),
    KiaKiaToOtherBanks(22, "KiaKia To Other Banks"),
    AccountLiquidation(24, "Account Liquidation"),
    BVNUpdate(25, "BVN Update"),
    MFBToOtherBanks(26, "MFB To Other Banks"),
    BetaHealthRegistration(27, "Beta Health Registration"),
    BetaHealthSubPayment(28, "Beta Health SubPayment"),
    POSCashOut(29, "POS Cash Out"),
    Nothing(16, "No Transactions Enabled"),
    PayWithTransfer(40, "Pay With Transfer"),
    CollectionPayment(32, "Collections");

    companion object {

        fun find(index: Int): TransactionType {

            return values().find { it.code == index }
                ?: throw IndexOutOfBoundsException("No transaction type exists for code $index")
        }
    }
}