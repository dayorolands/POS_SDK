package com.appzonegroup.creditclub.pos.printer

import com.appzonegroup.creditclub.pos.BuildConfig


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/10/2019.
 * Appzone Ltd
 */

val footerNodes = listOf<PrintNode>(

    TextNode("-----------------------------").apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode("CreditClub POS v${BuildConfig.VERSION_NAME}. Powered by CreditClub").apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode("http://www.appzonegroup.com/products/creditclub").apply {
        align = Alignment.MIDDLE
        walkPaperAfterPrint = 10
        wordFont = 15
    }
)