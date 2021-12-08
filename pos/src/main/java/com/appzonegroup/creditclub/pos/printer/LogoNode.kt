package com.appzonegroup.creditclub.pos.printer

import com.appzonegroup.creditclub.pos.R
import com.cluster.pos.printer.Alignment
import com.cluster.pos.printer.ImageNode
import com.cluster.pos.printer.PrintJobScope


@Suppress("FunctionName")
fun LogoNode(
    walkPaperAfterPrint: Int = 20,
    align: Alignment = Alignment.MIDDLE,
    printGray: Int = 5,
) = ImageNode(
    drawable = R.drawable.cc_printer_logo,
    walkPaperAfterPrint = walkPaperAfterPrint,
    align = align,
    printGray = printGray,
)

fun PrintJobScope.logo(
    walkPaperAfterPrint: Int = 20,
    align: Alignment = Alignment.MIDDLE,
    printGray: Int = 5,
) = image(
    drawable = R.drawable.cc_printer_logo,
    walkPaperAfterPrint = walkPaperAfterPrint,
    align = align,
    printGray = printGray,
)