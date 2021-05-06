package com.creditclub.pos.printer

import androidx.annotation.DrawableRes

interface PrintJob {
    val nodes: List<PrintNode>
}

class PrintJobScope {
    private var nodes = mutableListOf<PrintNode>()

    fun image(
        @DrawableRes drawable: Int,
        walkPaperAfterPrint: Int = 20,
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
        fontSize: Int = 2,
        printGray: Int = 5,
        isBold: Boolean = false,
        align: Alignment = Alignment.LEFT,
        walkPaperAfterPrint: Int = 0,
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

    fun build() = object : PrintJob {
        override val nodes: List<PrintNode> = this@PrintJobScope.nodes
    }
}

inline fun printJob(crossinline init: PrintJobScope.() -> Unit): PrintJob {
    return PrintJobScope().apply(init).build()
}