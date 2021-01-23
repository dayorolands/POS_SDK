package com.creditclub.pos.printer

import android.content.Context
import com.creditclub.core.ui.widget.DialogProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 1/14/2019.
 * Appzone Ltd
 */
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