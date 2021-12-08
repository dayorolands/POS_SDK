package com.cluster.pos.printer

import android.content.Context
import com.cluster.core.ui.widget.DialogProvider

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
}
