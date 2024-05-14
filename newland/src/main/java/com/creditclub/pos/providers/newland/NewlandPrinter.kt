package com.creditclub.pos.providers.newland

import android.content.Context
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.printer.PrintJob
import com.cluster.pos.printer.PrintNode
import com.cluster.pos.printer.PrinterStatus
import com.newland.nsdk.core.api.common.ModuleType
import com.newland.nsdk.core.api.internal.printer.Printer
import com.newland.nsdk.core.api.internal.printer.PrintingResultListener
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NewlandPrinter(
    override val context: Context,
    override val dialogProvider: DialogProvider
) : PosPrinter {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    val printer = NSDKModuleManagerImpl.getInstance().getModule(ModuleType.PRINTER) as Printer

    override fun check(): PrinterStatus = runBlocking {
        withContext(Dispatchers.IO){
            val status = translatePrinterStatus(printer.status.code)
            status
        }
    }

    private val iPrinterListener = object : PrintingResultListener {
        override fun onEventRaised(status: Int) {

        }
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus {
        TODO("Not yet implemented")
    }

    private fun translatePrinterStatus(code : Int) = when(code){
        0 -> PrinterStatus.READY
        2 -> PrinterStatus.NO_PAPER
        8 -> PrinterStatus.BUSY
        4 -> PrinterStatus.OVER_HEAT
        113 -> PrinterStatus.NO_COMMUNICATION
        else -> PrinterStatus.NOT_READY
    }
}