package com.cluster.pos.providers.smartpeak.p1000

import android.content.Context
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.printer.PrintJob
import com.cluster.pos.printer.PrintNode
import com.cluster.pos.printer.PrinterStatus

class SmartPeakPrinter(override val context: Context, override val dialogProvider: DialogProvider) :
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
}
