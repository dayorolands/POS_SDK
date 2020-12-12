package com.dspread.qpos

import android.Manifest
import android.content.Context.PRINT_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.printer.*
import com.dspread.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*


class QposPrinter(
    override val context: CreditClubActivity,
    override val dialogProvider: DialogProvider = context.dialogProvider
) : PosPrinter {
    override fun check(): PrinterStatus {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )
        return PrinterStatus.READY
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?
    ) {
        GlobalScope.launch {
            val printJob = object : PrintJob {
                override val nodes = nodes
            }
            print(printJob, message, false)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus {
        val printAttributes =
            PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(Resolution("res1", PRINT_SERVICE, 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
        val resolution = printAttributes.resolution
        val hdpi = resolution!!.horizontalDpi
        val vdpi = resolution.verticalDpi

        val pageWidth = 100
        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(pageWidth, 170, 1).create()

        val page = document.startPage(pageInfo)
        val x = 10F
        var y = 25F
        val paint = Paint()
        val canvas = page.canvas
        canvas.scale(90f / hdpi, 90f / vdpi)

        fun getXFromAlignment(width: Int, alignment: Alignment): Float = when (alignment) {
            Alignment.MIDDLE -> (pageWidth - width) / 2F
            Alignment.RIGHT -> pageWidth - width - 10F
            else -> x
        }

        fun walkPaper(distance: Int) {
            if (distance > 0) {
                y += distance / 3
            }
        }

        fun printText(node: TextNode) {
            node.text.split("\n").forEach { line ->
                canvas.drawText(line, x, y, paint)
                y += paint.descent() - paint.ascent()
            }

            walkPaper(node.walkPaperAfterPrint)
        }

        fun printImage(node: ImageNode) {
            val bitmap = BitmapFactory.decodeResource(context.resources, node.drawable)
                .scale(100, 100)
            canvas.drawBitmap(bitmap, x, y, paint)
            y += paint.descent() - paint.ascent() + bitmap.height
            walkPaper(node.walkPaperAfterPrint)
        }

        for (node in printJob.nodes) when (node) {
            is TextNode -> printText(node)
            is ImageNode -> printImage(node)
            is WalkPaper -> walkPaper(node.walkPaperAfterPrint)
        }

        document.finishPage(page)
        val file = safeRunIO {
            val myFilePath = context.externalCacheDir?.path + "/receipt-${UUID.randomUUID()}.pdf"
            val myFile = File(myFilePath)
            document.writeTo(FileOutputStream(myFile))
            myFile
        }.data ?: return PrinterStatus.READY
        document.close()

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            type = "application/pdf"
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.resources.getText(R.string.share_receipt_to)
            )
        )

        return PrinterStatus.READY
    }
}