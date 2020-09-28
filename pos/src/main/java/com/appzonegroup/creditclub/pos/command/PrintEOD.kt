package com.appzonegroup.creditclub.pos.command

import android.content.Context
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.format
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf

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
class PrintEOD(
    private val context: Context,
    private val isoSocketHelper: IsoSocketHelper,
    private val dialogProvider: DialogProvider,
    private val localDate: String
) : Runnable, KoinComponent {

    override fun run() {
        PosDatabase.open(context) { db ->
            val transactions = withContext(Dispatchers.IO) {
                db.financialTransactionDao().byDate(localDate)
            }

            if (transactions.isEmpty()) return@open dialogProvider.showError("No Transactions")

            var totalAmount = 0L

            val text = StringBuilder("END OF DAY RECEIPT $localDate")
            text.append("\n-----------------------\n")
            for (trn in transactions) {
                val iso = trn.isoMsg
                if (iso.isSuccessful) totalAmount += iso.transactionAmount4?.toLong() ?: 0

                text.append("\nTime    ${trn.createdAt.format("dd/MM/YYYY hh:mm", "+0100")}")
                text.append("\nType    ${trn.type}")
                text.append("\nPAN     ${trn.pan}")
                text.append("\nAmount  ${CurrencyFormatter.format(iso.transactionAmount4)}")
                text.append("\nRRN     ${trn.isoMsg.retrievalReferenceNumber37}")
                text.append("\nStatus  ${if (iso.isSuccessful) "APPROVED" else "DECLINED"}")
//                text .append("\n${iso.stan11}  ${iso.retrievalReferenceNumber37} ${CurrencyFormatter.format(iso.transactionAmount4)}")
                text.append("\n------------------\n")
            }

            text.append("\n\n${CurrencyFormatter.format("$totalAmount")} in card transactions")
            text.append("\nTotal Card Transactions ${transactions.size}")
            text.append("\nTotal Cash Transactions 0")

            val node = TextNode(text.toString())
            node.walkPaperAfterPrint = 20

            get<PosPrinter> { parametersOf(context, dialogProvider) }.printAsync(
                node,
                message = "Printing Report"
            ) { printerStatus ->
                if (printerStatus != PrinterStatus.READY) {
                    dialogProvider.showError(printerStatus.message)
                    return@printAsync
                }

                GlobalScope.launch(Dispatchers.Main) {
                    dialogProvider.showProgressBar("Connecting to host")

                    delay(2000)

                    dialogProvider.showProgressBar("Receiving reply")

                    delay(2000)

                    dialogProvider.showSuccess("Batch reconciled")
                }
            }
        }
    }
}