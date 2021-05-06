package com.appzonegroup.creditclub.pos.printer

import com.appzonegroup.creditclub.pos.R
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.ImageNode


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