package com.appzonegroup.creditclub.pos.command

import android.content.Context
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.contract.DialogProvider
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import kotlinx.coroutines.*

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
) : PosCommand() {

    override fun run() {
        PosDatabase.open(context) { db ->
            val transactions = withContext(Dispatchers.Default) {
                db.financialTransactionDao().byDate(localDate)
            }

            if (transactions.isEmpty()) return@open dialogProvider.showError("No Transactions")

            val text = StringBuilder("END OF DAY RECEIPT $localDate")
            text.append("\n-----------------------\n")
            for (trn in transactions) {
                val iso = trn.isoMsg

                text.append("\nTime    ${trn.prettyTime}")
                text.append("\nType    ${trn.type}")
                text.append("\nPAN     ${trn.pan}")
                text.append("\nAmount  ${CurrencyFormatter.format(iso.transactionAmount4)}")
                text.append("\nRRN     ${trn.isoMsg.retrievalReferenceNumber37}")
                text.append("\nStatus  ${if (iso.responseCode39 == "00") "APPROVED" else "DECLINED"}")
//                text .append("\n${iso.stan11}  ${iso.retrievalReferenceNumber37} ${CurrencyFormatter.format(iso.transactionAmount4)}")
                text.append("\n------------------\n")
            }

            text.append("\n\nTotal Card Transactions ${transactions.size}")
            text.append("\nTotal Cash Transactions 0")

            val node = TextNode(text.toString())
            node.walkPaperAfterPrint = 20

            PosPrinter(context, dialogProvider).printAsync(node, message = "Printing Report") { printerStatus ->
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