package com.cluster.pos.printer

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

interface PrintJob {
    val nodes: List<PrintNode>
}

interface ParcelablePrintJob : PrintJob, Parcelable

@Parcelize
@JvmInline
value class SimplePrintJob(override val nodes: List<PrintNode>) : ParcelablePrintJob

@JvmInline
value class PrintJobScope(private val nodes: MutableList<PrintNode> = mutableListOf()) {
    fun image(
        @DrawableRes drawable: Int,
        walkPaperAfterPrint: Int = 5,
        align: Alignment = Alignment.MIDDLE,
        printGray: Int = 5,
    ) {
        val imageNode = ImageNode(
            drawable = drawable,
            walkPaperAfterPrint = walkPaperAfterPrint,
            align = align,
            printGray = printGray,
        )
        nodes.add(imageNode)
    }

    fun text(
        text: String,
        leftDistance: Int = 0,
        lineDistance: Int = 0,
        fontSize: Int = 20,
        printGray: Int = 5,
        isBold: Boolean = false,
        align: Alignment = Alignment.LEFT,
        walkPaperAfterPrint: Int = 6,
    ) {
        val textNode = TextNode(
            text = text,
            leftDistance = leftDistance,
            lineDistance = lineDistance,
            wordFont = fontSize,
            printGray = printGray,
            isBold = isBold,
            align = align,
            walkPaperAfterPrint = walkPaperAfterPrint,
        )
        nodes.add(textNode)
    }

    fun walkPaper(distance: Int = 20) {
        nodes.add(WalkPaper(distance))
    }

    fun build(): ParcelablePrintJob = SimplePrintJob(this@PrintJobScope.nodes)
}

fun printJob(nodes: List<PrintNode>) = SimplePrintJob(nodes)

inline fun printJob(crossinline init: PrintJobScope.() -> Unit): ParcelablePrintJob {
    return PrintJobScope().apply(init).build()
}