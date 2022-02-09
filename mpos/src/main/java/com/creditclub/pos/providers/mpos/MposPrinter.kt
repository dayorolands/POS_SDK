package com.cluster.pos.providers.mpos

import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.printer.PrintJob
import com.cluster.pos.printer.PrintNode
import com.cluster.pos.printer.PrinterStatus

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