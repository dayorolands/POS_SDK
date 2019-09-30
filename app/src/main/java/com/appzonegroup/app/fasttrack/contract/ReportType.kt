package com.appzonegroup.app.fasttrack.contract


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 7/1/2019.
 * Appzone Ltd
 */
enum class ReportType(val id: Int) {
    BalanceEnquiry(1),
    MiniStatement(2),
    Recharge(3),
    BillsPayment(4),
    CashIn(5),
    CashOut(6),
    PINChange(7),
    PINReset(8),
    Registration(9),
    FundsTransferCommercialBank(10),
    FundsTransferCashIn(11),
    FundsTransferCashOut(12),
    FundsTransferLocal(13),
    AccountActivation(14),
    LocalFundsTransfer(15),
    SetDefaultAccount(16),
    LoanRequest(17),
    CardLinking(18),
    AgentToAgentFundsTransfer(19),
    KiaKiaToKiaKia(20),
    KiaKiaToSterlingBank(21),
    KiaKiaToOtherBanks(22),
    AccountLiquidation(23),
    POSCashOut(24);

    companion object {

        operator fun get(id: String): ReportType? = get(id.toInt())

        operator fun get(id: Int): ReportType? {
            return when (id) {
                1 -> BalanceEnquiry
                2 -> MiniStatement
                3 -> Recharge
                4 -> BillsPayment
                5 -> CashIn
                6 -> CashOut
                7 -> PINChange
                8 -> PINReset
                9 -> Registration
                10 -> FundsTransferCommercialBank
                11 -> FundsTransferCashIn
                12 -> FundsTransferCashOut
                13 -> FundsTransferLocal
                14 -> AccountActivation
                15 -> LocalFundsTransfer
                16 -> SetDefaultAccount
                17 -> LoanRequest
                18 -> CardLinking
                19 -> AgentToAgentFundsTransfer
                20 -> KiaKiaToKiaKia
                21 -> KiaKiaToSterlingBank
                22 -> KiaKiaToOtherBanks
                23 -> AccountLiquidation
                24 -> POSCashOut
                else -> null
            }
        }
    }
}


fun getReportType(id: String): ReportType? = getReportType(id.toInt())

fun getReportType(id: Int): ReportType? {
    return when (id) {
        1 -> ReportType.BalanceEnquiry
        2 -> ReportType.MiniStatement
        3 -> ReportType.Recharge
        4 -> ReportType.BillsPayment
        5 -> ReportType.CashIn
        6 -> ReportType.CashOut
        7 -> ReportType.PINChange
        8 -> ReportType.PINReset
        9 -> ReportType.Registration
        10 -> ReportType.FundsTransferCommercialBank
        11 -> ReportType.FundsTransferCashIn
        12 -> ReportType.FundsTransferCashOut
        13 -> ReportType.FundsTransferLocal
        14 -> ReportType.AccountActivation
        15 -> ReportType.LocalFundsTransfer
        16 -> ReportType.SetDefaultAccount
        17 -> ReportType.LoanRequest
        18 -> ReportType.CardLinking
        19 -> ReportType.AgentToAgentFundsTransfer
        20 -> ReportType.KiaKiaToKiaKia
        21 -> ReportType.KiaKiaToSterlingBank
        22 -> ReportType.KiaKiaToOtherBanks
        23 -> ReportType.AccountLiquidation
        24 -> ReportType.POSCashOut
        else -> null
    }
}