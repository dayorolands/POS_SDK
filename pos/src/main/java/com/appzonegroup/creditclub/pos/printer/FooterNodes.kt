package com.appzonegroup.creditclub.pos.printer

import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/10/2019.
 * Appzone Ltd
 */

val footerNodes = listOf<PrintNode>(

    TextNode("-----------------------------").apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode("CreditClub POS v1.0.1. Powered by CreditClub")
        .apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode("http://www.appzonegroup.com/products/creditclub")
        .apply {
        align = Alignment.MIDDLE
        walkPaperAfterPrint = 10
        wordFont = 15
    }
)