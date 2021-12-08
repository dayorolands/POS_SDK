package com.cluster.pos.printer

import com.cluster.pos.R


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