package com.creditclub.pos.providers.mpos

import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.PrinterStatus

class MposPrinter(override val context: CreditClubActivity) : PosPrinter {
    override val dialogProvider: DialogProvider = context.dialogProvider

    override fun check(): PrinterStatus = PrinterStatus.READY
    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?
    ) {
        block?.invoke(PrinterStatus.READY)
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus = PrinterStatus.READY
}