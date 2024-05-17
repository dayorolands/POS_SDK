package com.orda.horizonpay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.util.Log
import android.util.Printer
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.*
import com.horizonpay.smartpossdk.aidl.printer.AidlPrinterListener
import com.horizonpay.smartpossdk.aidl.printer.IAidlPrinter
import com.horizonpay.smartpossdk.data.PrinterConst
import com.horizonpay.smartpossdk.data.PrinterConst.AlignMode
import com.orda.horizonpay.printer.CombBitmap
import com.orda.horizonpay.printer.GenerateBitmap
import kotlinx.coroutines.*

class HorizonPrinter(
    override val context: Context,
    override val dialogProvider: DialogProvider
) : PosPrinter{
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val combBitmap = CombBitmap()
    private val devicePrinter : IAidlPrinter = HorizonDeviceSingleton.getDevice()!!.printer
    private val assetManager = context.assets
    private val fontType : Typeface? = Typeface.createFromAsset(assetManager, "fonts/Fangsong.ttf")

    override fun check(): PrinterStatus = runBlocking {
        withContext(Dispatchers.IO){
            val status = translateStatus(devicePrinter!!.printerState)
            status
        }
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?
    ) {
        mainScope.launch {
            val status = print(printJob(nodes))
            block?.invoke(status)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus = withContext(Dispatchers.IO){
        if(devicePrinter.isSupport){
            devicePrinter.pushPaper()
            for(node in printJob.nodes) {
                when(node){
                    is ImageNode -> imageNode(node)
                    is TextNode -> textNode(node)
                    is WalkPaper -> walkpaper(node.walkPaperAfterPrint)
                }
            }
        }
        val printBitMap = combBitmap.combBitmap
        devicePrinter.printBmp(true, true, printBitMap, 0, aidlPrinterListener)
        val status = translateStatus(devicePrinter.printerState)
        status
    }

    private fun imageNode(node: ImageNode){
        val bitMap : Bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        combBitmap.addBitmap(bitMap)
    }

    private fun walkpaper(distance: Int) {
        if(distance > 0){
            combBitmap.addBitmap(
                GenerateBitmap
                    .str2Bitmap(
                        "\n".repeat(distance/3),
                        1,
                        GenerateBitmap.AlignEnum.CENTER,
                        fontType,
                        false,
                        false
                    )
            )
        }
    }

    private fun textNode(node: TextNode){
        devicePrinter.printGray = PrinterConst.Gray.LEVEL_4
        combBitmap.addBitmap(
            GenerateBitmap
                .str2Bitmap(
                    node.text,
                    node.wordFont,
                    node.align.asAlignEnum,
                    fontType,
                    node.isBold,
                    false
                )
        )
        walkpaper(node.walkPaperAfterPrint)
    }

    private val aidlPrinterListener: AidlPrinterListener = object : AidlPrinterListener.Stub() {
        override fun onError(i: Int) {
            when(i){
                PrinterConst.RetCode.ERROR_DEV_IS_BUSY ->
                    return
                PrinterConst.RetCode.ERROR_DEV ->
                    return
                PrinterConst.RetCode.ERROR_PRINT_NOPAPER ->
                    return
                PrinterConst.RetCode.ERROR_OTHER ->
                    return
            }
        }

        override fun onPrintSuccess() {
            combBitmap.removeAll()
        }
    }

    private fun translateStatus(code: Int) = when(code){
        PrinterConst.State.PRINTER_STATE_NORMAL -> PrinterStatus.READY
        PrinterConst.State.PRINTER_STATE_BUSY -> PrinterStatus.NOT_READY
        PrinterConst.State.PRINTER_STATE_NOPAPER -> PrinterStatus.NO_PAPER
        PrinterConst.State.PRINTER_STATE_HIGHTEMP -> PrinterStatus.OVER_HEAT
        PrinterConst.State.ERROR_BATTERY_LOW -> PrinterStatus.LOW_BATTERY
        else -> PrinterStatus.NOT_READY
    }

    private inline val Alignment.asAlignEnum
        get() = when(this){
            Alignment.LEFT -> GenerateBitmap.AlignEnum.LEFT
            Alignment.MIDDLE -> GenerateBitmap.AlignEnum.CENTER
            Alignment.RIGHT -> GenerateBitmap.AlignEnum.RIGHT
        }
}