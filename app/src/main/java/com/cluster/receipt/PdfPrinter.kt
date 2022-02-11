package com.cluster.receipt

import android.Manifest
import android.app.Activity
import android.content.Context.PRINT_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.cluster.R
import com.cluster.core.data.api.AppConfig
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.pos.printer.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.time.Instant


class PdfPrinter(
    override val context: Activity,
    override val dialogProvider: DialogProvider,
    private val appConfig: AppConfig,
) : PosPrinter {
    private val mainScope = MainScope()

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
        block: ((PrinterStatus) -> Unit)?,
    ) {
        mainScope.launch {
            print(printJob(nodes), message, false)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean,
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
        val time = Instant.now().format("ddMMHHmmss")
        val (file) = safeRunIO {
            val receiptFolder = File(context.filesDir.path, "receipts")
            if (!receiptFolder.exists()) {
                receiptFolder.mkdir()
            }
            val receiptFilePath = "${receiptFolder.path}/${appConfig.appName} receipt ${time}.pdf"
            val receiptFile = File(receiptFilePath).apply {
                createNewFile()
                outputStream().use { outputStream ->
                    document.writeTo(outputStream)
                }
            }

            receiptFile
        }
        document.close()
        if (file == null) {
            return PrinterStatus.NOT_READY
        }

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uri = FileProvider.getUriForFile(
                context.applicationContext,
                appConfig.fileProviderAuthority,
                file,
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setDataAndType(uri, context.contentResolver.getType(uri))
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            //GRANTING THE PERMISSIONS EXPLICITLY HERE! to all possible choosers (3rd party apps):
            val resolvedInfoActivities: List<ResolveInfo> = context.packageManager
                .queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)

            for (ri in resolvedInfoActivities) {
                context.grantUriPermission(
                    ri.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        } else {
            val uri = Uri.fromFile(file)
            shareIntent.setDataAndType(uri, context.contentResolver.getType(uri))
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.resources.getText(R.string.share_receipt_to)
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )

        return PrinterStatus.READY
    }
}