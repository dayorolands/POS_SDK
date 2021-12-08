package pos.providers.wizar

import android.content.Context
import android.graphics.BitmapFactory
import com.cloudpos.POSTerminal
import com.cloudpos.printer.Format
import com.cloudpos.printer.PrinterDevice
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.*
import kotlinx.coroutines.*

class WizarPrinter(override val context: Context, override val dialogProvider: DialogProvider) :
    PosPrinter {

    private val printer = POSTerminal.getInstance(context)
        .getDevice("cloudpos.device.printer") as PrinterDevice
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun check(): PrinterStatus = runBlocking {
        withContext(Dispatchers.IO) {
            printer.open()
            val status = translateStatus(printer.queryStatus())
            printer.close()
            status
        }
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?,
    ) {
        mainScope.launch {
            val status = print(printJob(nodes))
            block?.invoke(status)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean,
    ): PrinterStatus = withContext(Dispatchers.IO) {
        printer.open()
        val format = Format()
        for (node in printJob.nodes) {
            format.clear()
            val alignmentStr = when (node.align) {
                Alignment.MIDDLE -> Format.FORMAT_ALIGN_CENTER
                Alignment.RIGHT -> Format.FORMAT_ALIGN_RIGHT
                else -> Format.FORMAT_ALIGN_LEFT
            }
            format.setParameter(Format.FORMAT_ALIGN, alignmentStr)
            when (node) {
                is TextNode -> {
                    val fontSize = when {
                        node.wordFont > 30 -> Format.FORMAT_FONT_SIZE_EXTRALARGE
                        node.wordFont > 25 -> Format.FORMAT_FONT_SIZE_LARGE
                        node.wordFont > 20 -> Format.FORMAT_FONT_SIZE_MEDIUM
                        node.wordFont > 14 -> Format.FORMAT_FONT_SIZE_SMALL
                        else -> Format.FORMAT_FONT_SIZE_EXTRASMALL
                    }
                    format.setParameter(Format.FORMAT_FONT_SIZE, fontSize)
                    format.setParameter(
                        Format.FORMAT_FONT_BOLD,
                        if (node.isBold) Format.FORMAT_FONT_VAL_TRUE else Format.FORMAT_FONT_VAL_FALSE
                    )
                    printer.printText(format, node)
                }
                is ImageNode -> {
                    printer.printImage(format, node)
                }
                is WalkPaper -> printer.walkPaper(node.walkPaperAfterPrint)
                else -> {
                }
            }
        }

        val status = translateStatus(printer.queryStatus())
        printer.close()
        status
    }

    private fun translateStatus(code: Int): PrinterStatus = when (code) {
        PrinterDevice.STATUS_PAPER_EXIST -> PrinterStatus.READY
        PrinterDevice.STATUS_OUT_OF_PAPER -> PrinterStatus.NO_PAPER
        else -> PrinterStatus.NOT_READY
    }

    private fun PrinterDevice.walkPaper(distance: Int) {
        if (distance > 0) {
            printlnText("\n".repeat(distance / 3))
        }
    }

    private fun PrinterDevice.printText(format: Format, node: TextNode) {
        printlnText(format, node.text)
        walkPaper(node.walkPaperAfterPrint)
    }

    private fun PrinterDevice.printImage(format: Format, node: ImageNode) {
        val bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        printBitmap(format, bitmap)
    }
}
