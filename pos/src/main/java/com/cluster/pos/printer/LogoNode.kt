package com.cluster.pos.printer

import com.cluster.pos.R


@Suppress("FunctionName")
fun LogoNode(
    walkPaperAfterPrint: Int = 20,
    align: Alignment = Alignment.MIDDLE,
    printGray: Int = 5,
) = ImageNode(
    drawable = R.drawable.orda_logo_dark_payment,
    walkPaperAfterPrint = walkPaperAfterPrint,
    align = align,
    printGray = printGray,
)

fun PrintJobScope.logo(
    walkPaperAfterPrint: Int = 20,
    align: Alignment = Alignment.MIDDLE,
    printGray: Int = 5,
) = image(
    drawable = R.drawable.orda_logo_dark_payment,
    walkPaperAfterPrint = walkPaperAfterPrint,
    align = align,
    printGray = printGray,
)