package com.creditclub.pos.printer

import android.content.Context
import com.creditclub.core.ui.widget.DialogProvider

interface PosPrinter {
    val context: Context
    val dialogProvider: DialogProvider

    fun check(): PrinterStatus

    fun printAsync(
        printJob: PrintJob,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    ) {
        printAsync(printJob.nodes, message, block)
    }

    fun printAsync(
        vararg nodes: PrintNode,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    ) {
        printAsync(nodes.asList(), message, block)
    }

    fun printAsync(
        nodes: List<PrintNode>,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    )

    suspend fun print(
        printJob: PrintJob,
        message: String = "Printing...",
        retryOnFail: Boolean = true
    ): PrinterStatus
}