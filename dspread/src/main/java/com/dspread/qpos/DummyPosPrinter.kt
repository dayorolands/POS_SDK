package com.dspread.qpos

import android.content.Context
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.PrinterStatus

class DummyPosPrinter(
    override val context: CreditClubActivity,
    override val dialogProvider: DialogProvider = context.dialogProvider
) : PosPrinter {
    override fun check(): PrinterStatus {
        return PrinterStatus.READY
    }

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
    ): PrinterStatus {
        return PrinterStatus.READY
    }
}