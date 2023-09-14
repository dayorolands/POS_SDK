package com.creditclub.pos.providers.sunmi

import android.content.Context
import android.graphics.BitmapFactory
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.*
import com.sunmi.peripheral.printer.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.IllegalArgumentException

class SunmiPrinter(override val context: Context, override val dialogProvider: DialogProvider) : PosPrinter {
    var sunmiPrinterService : SunmiPrinterService? = null
    private var isBindPrinterServiceCalled : Boolean = false
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val lock = Any()

    init {
        runBlocking {
            synchronized(lock = lock){
                bindPrintService()
                isBindPrinterServiceCalled = true
            }
        }
    }

    override fun check(): PrinterStatus = runBlocking {
        withContext(Dispatchers.IO){
            synchronized(lock = lock){
                if(!isBindPrinterServiceCalled){
                    throw IllegalStateException("Bind printer service must be called first")
                }

                sunmiPrinterService?.enterPrinterBuffer(true)
                val status = translateStatus(
                    sunmiPrinterService?.updatePrinterState() ?: 80
                )
                sunmiPrinterService?.exitPrinterBuffer(true)
                status
            }
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
        sunmiPrinterService?.enterPrinterBuffer(true)
        for (node in printJob.nodes) when (node) {
            is TextNode -> {
                textNode(node)
            }
            is ImageNode -> {
                imageNode(node)
            }
            is WalkPaper -> {
                walkpaper(node.walkPaperAfterPrint)
            }
            is TextNodeWithNewLine -> {
                textNodeWithNewLine(node)
            }
        }
        val status = translateStatus(sunmiPrinterService?.updatePrinterState() ?: 80)
        sunmiPrinterService?.exitPrinterBuffer(true)
        status
    }

    private fun textNode(node: TextNode){
        sunmiPrinterService?.setAlignment(node.align.asAlignEnum, innerResultCallback)
        sunmiPrinterService?.setFontSize(node.wordFont.toFloat(), innerResultCallback)
        sunmiPrinterService?.printText(
            node.text,
            innerResultCallback
        )
        walkpaper(node.walkPaperAfterPrint)
    }

    private fun imageNode(node: ImageNode) {
        val bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        sunmiPrinterService?.setAlignment(node.align.asAlignEnum, null)
        sunmiPrinterService?.printBitmap(bitmap, innerResultCallback)
    }

    private fun textNodeWithNewLine(node: TextNodeWithNewLine){
        sunmiPrinterService?.setAlignment(node.align.asAlignEnum, innerResultCallback)
        sunmiPrinterService?.setFontSize(node.wordFont.toFloat(), innerResultCallback)
        sunmiPrinterService?.printText(
            node.text + "\n",
            innerResultCallback
        )
    }

    private fun walkpaper(distance: Int){
        if(distance > 0){
            sunmiPrinterService?.printText(
                "\n".repeat(distance/3),
                innerResultCallback
            )
        }
    }

    private fun translateStatus(code: Int): PrinterStatus = when(code){
        1 -> PrinterStatus.READY
        3 -> PrinterStatus.NO_COMMUNICATION
        4 -> PrinterStatus.NO_PAPER
        5 -> PrinterStatus.OVER_HEAT
        80 -> PrinterStatus.RETRY
        else -> {
            PrinterStatus.NOT_READY
        }
    }

    private fun bindPrintService(){
        try{
            InnerPrinterManager.getInstance().bindService(context, object : InnerPrinterCallback(){
                override fun onConnected(service: SunmiPrinterService?) {
                    sunmiPrinterService = service
                    Log.d("SunmiPayKernel", "the value of sunmiprinterservice at this point : ${sunmiPrinterService?.printerVersion}")
                }

                override fun onDisconnected() {
                    sunmiPrinterService = null
                    Log.d("SunmiPayKernel", "the value of sunmiprinterservice at this point 2 : $sunmiPrinterService")
                }

            })
        } catch (e: InnerPrinterException){
            e.printStackTrace()
        }
    }

    private var `is` = true
    private val innerResultCallback: InnerResultCallbcak = object : InnerResultCallbcak() {
        override fun onRunResult(isSuccess: Boolean) {
            Log.d("SunmiPayKernel", "isSuccess:$isSuccess")
            if (`is`) {
                try {
                    sunmiPrinterService!!.printTextWithFont(
                        "\n",
                        "",
                        0F,
                        this
                    )
                    sunmiPrinterService!!.lineWrap(4, this)
                    `is` = false
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onReturnString(result: String) {
            Log.d("SunmiPayKernel", "isSuccess:$result")
        }

        override fun onRaiseException(code: Int, msg: String) {
            Log.d("SunmiPayKernel", "code:$code,msg:$msg")
        }

        override fun onPrintResult(code: Int, msg: String) {
            Log.d("SunmiPayKernel", "code:$code,msg:$msg")
        }
    }

    private inline val Alignment.asAlignEnum
        get() = when(this){
            Alignment.LEFT -> 0
            Alignment.MIDDLE -> 1
            Alignment.RIGHT -> 2
        }
}