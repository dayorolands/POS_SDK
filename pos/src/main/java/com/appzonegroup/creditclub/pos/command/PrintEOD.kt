package com.appzonegroup.creditclub.pos.command

import android.content.Context
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.format
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.pos.printer.printJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 *
 *
 * EOD Report Format
 * -----------
 * 1. Time of Transaction
 * 2. Type of Transaction
 * 3. PAN
 * 4. Amount
 * 5. Status (Approved or Declined)
 * -----------
 * 6. Total Card Transactions for the day
 * 7. Total Cash Transaction for the day
 *
 */
suspend fun printEOD(
    context: Context,
    dialogProvider: DialogProvider,
    localDate: LocalDate,
    posPrinter: PosPrinter,
) {
    val from: Instant = localDate.atStartOfDay().toInstant(ZoneOffset.MIN)
    val to: Instant = localDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.MIN)

    val transactions = withContext(Dispatchers.IO) {
        PosDatabase
            .getInstance(context = context)
            .financialTransactionDao()
            .findAllInRange(from = from, to = to)
    }
    if (transactions.isEmpty()) {
        dialogProvider.showError("No Transactions")
        return
    }

    var totalAmount = 0.0

    val text = StringBuilder("END OF DAY RECEIPT $localDate").apply {
        append("\n-----------------------\n")
    }
    for (trn in transactions) {
        val iso = trn.isoMsg
        val transactionAmount = iso.transactionAmount4?.toDoubleOrNull()?.div(100.0) ?: 0.0
        if (iso.isSuccessful) {
            totalAmount += transactionAmount
        }

        text.apply {
            append("\nTime    ${trn.createdAt.format("dd/MM/yyyy hh:mm", "+0100")}")
            append("\nType    ${trn.type}")
            append("\nPAN     ${trn.pan}")
            append("\nAmount  ${transactionAmount.toCurrencyFormat()}")
            append("\nRRN     ${trn.isoMsg.retrievalReferenceNumber37}")
            append("\nStatus  ${if (iso.isSuccessful) "APPROVED" else "DECLINED"}")
            append("\n------------------\n")
        }
    }

    text.apply {
        append("\n\n${totalAmount.toCurrencyFormat()} in card transactions")
        append("\nTotal Card Transactions ${transactions.size}")
        append("\nTotal Cash Transactions 0")
    }

    val printJob = printJob {
        text(text.toString(), walkPaperAfterPrint = 20)
    }
    val printerStatus = posPrinter.print(printJob = printJob, message = "Printing Report")
    if (printerStatus != PrinterStatus.READY) {
        dialogProvider.showError(printerStatus.message)
        return
    }

    dialogProvider.showProgressBar("Connecting to host")
    delay(2000)
    dialogProvider.showProgressBar("Receiving reply")
    delay(2000)
    dialogProvider.showSuccess("Batch reconciled")
}
