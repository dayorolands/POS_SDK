package com.appzonegroup.app.fasttrack.model

import com.creditclub.core.type.TransactionType
import com.creditclub.core.type.TransactionGroup


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 20/09/2019.
 * Appzone Ltd
 */
object PolarisTransactionGroup : TransactionGroup(
    listOf(
        TransactionType.CashIn,
        TransactionType.CashOut,
        TransactionType.POSCashOut,
        TransactionType.BalanceEnquiry,
        TransactionType.MiniStatement,
        TransactionType.Recharge,
        TransactionType.BillsPayment,
        TransactionType.PINReset,
        TransactionType.PINChange,
        TransactionType.Registration,
        TransactionType.FundsTransferCommercialBank,
        TransactionType.AccountLiquidation
    )
)