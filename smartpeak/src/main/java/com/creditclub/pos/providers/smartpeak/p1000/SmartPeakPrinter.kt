package com.creditclub.pos.providers.smartpeak.p1000

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.appzonegroup.creditclub.pos.printer.PrintJob
import com.appzonegroup.creditclub.pos.printer.PrintNode
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.ui.widget.DialogProvider

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

    override fun print(nodes: List<PrintNode>): PrinterStatus {
        return PrinterStatus.READY
    }
}
