package com.appzonegroup.app.fasttrack.receipt

import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.PrintNode
import com.appzonegroup.creditclub.pos.printer.TextNode


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 06/10/2019.
 * Appzone Ltd
 */

val polarisFooterNodes = listOf<PrintNode>(
    TextNode(
        """
-----------------------------

Please retain your receipt.
Thank You.

Polaris Agency Banking v${BuildConfig.VERSION_NAME}
-----------------------------"""
    ),

    TextNode("Powered by Polaris Bank").apply {
        align = Alignment.MIDDLE
    },

    TextNode("https://www.polarisbanklimited.com").apply {
        align = Alignment.MIDDLE
        walkPaperAfterPrint = 10
    }
)