package com.urovo.v67

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.Alignment
import com.cluster.pos.printer.ImageNode
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.printer.PrintJob
import com.cluster.pos.printer.PrintNode
import com.cluster.pos.printer.PrinterStatus
import com.cluster.pos.printer.TextNode
import com.cluster.pos.printer.WalkPaper
import com.cluster.pos.printer.printJob
import com.urovo.sdk.print.PrinterProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class UrovoPrinter(override val context: Context, override val dialogProvider: DialogProvider) : PosPrinter {
    private val printer = PrinterProviderImpl.getInstance(context)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        printer.initPrint()
    }

    override fun check(): PrinterStatus = runBlocking {
        withContext(Dispatchers.IO){
            printer.initPrint()
            val status = translateStatus(printer.status)
            printer.close()
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
        printer.initPrint()
        val format = Bundle()
        for (node in printJob.nodes) when (node) {
            is TextNode -> {
                val fontSize = when {
                    node.wordFont > 30 -> 2
                    node.wordFont > 25 -> 2
                    node.wordFont > 20 -> 1
                    node.wordFont > 14 -> 1
                    else -> 0
                }
                val fontPath = File(Environment.getExternalStorageDirectory(), "COMICATE.TTF").absolutePath
                format.putInt("font", fontSize)
                format.putInt("align", node.align.asAlignEnum)
                format.putBoolean("fontBold", node.isBold)
                format.putString("fontName", fontPath)
                format.putInt("lineHeight", 2)
                textNode(format, node)
            }
            is ImageNode -> {
                format.putInt("align", node.align.asAlignEnum)
                format.putInt("offset", 0)
                format.putInt("height", 300)
                imageNode(format, node)
            }
            is WalkPaper -> {
                walkPaper(node.walkPaperAfterPrint)
            }
        }
        printer.startPrint()
        printer.close()
        val status = translateStatus(printer.status)
        status
    }

    private fun textNode(format : Bundle , node : TextNode){
        printer.setGray(node.printGray)
        printer.addText(format,   node.text)
        //walkPaper(node.walkPaperAfterPrint)
    }

    private fun walkPaper(distance: Int) {
        if (distance > 0) {
            val format = Bundle()
            printer.addText(format, "\n".repeat(distance / 10))
        }
    }

    private fun imageNode(format: Bundle, node: ImageNode){
        val bitMap : Bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        printer.addImage(format, getBitmapBytes(bitMap))
    }

    private fun imageNodeBit(format : Bundle, node: ImageNode){
        val bitMap : Bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        printer.addImage(format, getBitmapBytes(bitMap))
    }

    private fun translateStatus(code: Int): PrinterStatus = when(code){
        0 -> PrinterStatus.READY
        240 -> PrinterStatus.NO_PAPER
        242 -> PrinterStatus.NO_COMMUNICATION
        243 -> PrinterStatus.OVER_HEAT
        247 -> PrinterStatus.BUSY
        else -> {
            PrinterStatus.NOT_READY
        }
    }

    private inline val Alignment.asAlignEnum
        get() = when(this){
            Alignment.LEFT -> 0
            Alignment.MIDDLE -> 1
            Alignment.RIGHT -> 2
        }

    private fun getBitmapBytes(bitmap: Bitmap): ByteArray? {
        var imageData: ByteArray? = null
        imageData = try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return imageData
    }
}