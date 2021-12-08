package com.nexgo.n3

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.*
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.printer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class N3Printer(override val context: Context, override val dialogProvider: DialogProvider) :
    PosPrinter {

    private val printer = APIProxy.getDeviceEngine(context).printer
    private val mainScope = CoroutineScope(Dispatchers.Main)

    init {
        printer.setTypeface(Typeface.DEFAULT)
    }

    override fun check(): PrinterStatus = translateStatus(printer.status)

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
    ): PrinterStatus {
        printer.initPrinter()
        printer.setTypeface(Typeface.DEFAULT)
        printer.setLetterSpacing(5)
        printer.setGray(GrayLevelEnum.LEVEL_2)

        for (node in printJob.nodes) when (node) {
            is TextNode -> printer.printText(node)
            is ImageNode -> printer.printImage(node)
            is WalkPaper -> printer.walkPaper(node.walkPaperAfterPrint)
        }

        return suspendCoroutine { continuation ->
            printer.startPrint(false) { retCode ->
                continuation.resume(translateStatus(retCode))
            }
        }
    }

    private fun translateStatus(code: Int): PrinterStatus = when (code) {
        0 -> PrinterStatus.READY
        SdkResult.Printer_PaperLack -> PrinterStatus.NO_PAPER
        SdkResult.Printer_TooHot -> PrinterStatus.OVER_HEAT
        SdkResult.Printer_AddImg_Fail -> PrinterStatus.IMAGE_NOT_FOUND
        else -> PrinterStatus.NOT_READY
    }

    private fun Printer.walkPaper(distance: Int) {
        if (distance > 0) {
            appendPrnStr("\n".repeat(distance / 3), 20, AlignEnum.CENTER, false)
        }
    }

    private fun Printer.printText(node: TextNode) {
        appendPrnStr(node.text, node.wordFont, node.align.asAlignEnum, false)
        walkPaper(node.walkPaperAfterPrint)
    }

    private fun Printer.printImage(node: ImageNode) {
        val bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
        appendImage(bitmap, node.align.asAlignEnum)
//        walkPaper(node.walkPaperAfterPrint)
    }

    private inline val FontWeight.fontEntity
        get() = when (this) {
            FontWeight.Small ->
                FontEntity(DotMatrixFontEnum.CH_SONG_20X20, DotMatrixFontEnum.ASC_SONG_8X16)
            FontWeight.Normal ->
                FontEntity(DotMatrixFontEnum.CH_SONG_24X24, DotMatrixFontEnum.ASC_SONG_12X24)
            FontWeight.Bold ->
                FontEntity(DotMatrixFontEnum.CH_SONG_24X24, DotMatrixFontEnum.ASC_SONG_BOLD_16X24)
            FontWeight.Big ->
                FontEntity(
                    DotMatrixFontEnum.CH_SONG_24X24,
                    DotMatrixFontEnum.ASC_SONG_12X24,
                    false,
                    true
                )
        }

    private inline val Alignment.asAlignEnum
        get() = when (this) {
            Alignment.LEFT -> AlignEnum.LEFT
            Alignment.MIDDLE -> AlignEnum.CENTER
            Alignment.RIGHT -> AlignEnum.RIGHT
        }
}
