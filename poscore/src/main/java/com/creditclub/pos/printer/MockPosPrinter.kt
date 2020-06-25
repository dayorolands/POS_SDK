package com.creditclub.pos.printer

import android.content.Context
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.ui.widget.DialogProvider

class MockPosPrinter(override val context: Context, override val dialogProvider: DialogProvider) :
    PosPrinter {
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

    override fun print(nodes: List<PrintNode>): PrinterStatus {
        return PrinterStatus.READY
    }
}
