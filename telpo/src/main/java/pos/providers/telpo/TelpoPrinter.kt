package pos.providers.telpo

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Looper
import android.widget.Toast
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.*
import com.telpo.tps550.api.printer.UsbThermalPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_LEFT_DISTANCE = 255

class TelpoPrinter(override val context: Context, override val dialogProvider: DialogProvider) :
    UsbThermalPrinter(context),
    PosPrinter {

    private val mainScope = MainScope()

    override fun check(): PrinterStatus {
        return try {
            start(0)
            reset()

            if (checkStatus() == 0) PrinterStatus.READY else PrinterStatus.NOT_READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?,
    ) {
        mainScope.launch {
            dialogProvider.showProgressBar(message)
            val status = withContext(Dispatchers.Default) {
                Looper.myLooper() ?: Looper.prepare()
                print(nodes)
            }
            dialogProvider.hideProgressBar()
            block?.invoke(status)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean,
    ): PrinterStatus {
        dialogProvider.showProgressBar(message)
        val status = withContext(Dispatchers.Default) {
            Looper.myLooper() ?: Looper.prepare()
            print(printJob.nodes)
        }
        dialogProvider.hideProgressBar()
        if (status == PrinterStatus.READY) return status
        if (!retryOnFail) {
            dialogProvider.showErrorAndWait(status.message)
            return status
        }

        val tryAgain = dialogProvider.getConfirmation(status.message, "Try again?")

        return if (tryAgain) print(printJob, message, false)
        else status
    }

    private fun print(nodes: List<PrintNode>): PrinterStatus {
        for (node in nodes) {
            val status: PrinterStatus = when (node) {
                is TextNode -> print(node)
                is ImageNode -> print(node)
                is WalkPaper -> print(node)
                else -> PrinterStatus.READY
            }

            if (status != PrinterStatus.READY) return status
        }

        return PrinterStatus.READY
    }

    private fun print(node: ImageNode): PrinterStatus {
        return try {
            reset()
            setGray(node.printGray)
            setAlgin(node.align.ordinal)
            val bm = BitmapFactory.decodeResource(context.resources, node.drawable)

            if (bm != null) {
                printLogo(bm, false)
                PrinterStatus.READY
            } else {
                PrinterStatus.IMAGE_NOT_FOUND
            }
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    private fun print(node: WalkPaper): PrinterStatus {
        return try {
            reset()
            walkPaper(node.walkPaperAfterPrint)
            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    private fun print(node: TextNode): PrinterStatus {
        if (node.leftDistance > MAX_LEFT_DISTANCE) {
            Toast.makeText(context, context.getString(R.string.outOfLeft), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.lineDistance > 255) {
            Toast.makeText(context, context.getString(R.string.outOfLine), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.wordFont > 55 || node.wordFont < 15) {
            Toast.makeText(context, context.getString(R.string.outOfFont), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.printGray < 0 || node.printGray > 7) {
            Toast.makeText(context, context.getString(R.string.outOfGray), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.text.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.empty), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        return try {
            reset()
            setAlgin(node.align.ordinal)
            setLeftIndent(node.leftDistance)
            setLineSpace(node.lineDistance)

            if (node.wordFont > 45) setFontSize(node.wordFont)
            else setTextSize(node.wordFont)

            setGray(node.printGray)
            addString(node.text)
            printString()
            walkPaper(node.walkPaperAfterPrint)

            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    private fun printerStatus(e: Exception) = when (e.toString()) {
        "com.telpo.tps550.api.NoPaperException" -> PrinterStatus.NO_PAPER
        "com.telpo.tps550.api.OverHeatException" -> PrinterStatus.OVER_HEAT
        "com.telpo.tps550.api.BlackBlockNotFoundException" -> PrinterStatus.NO_BLACK_BLOCK
        else -> PrinterStatus.NOT_READY
    }
}