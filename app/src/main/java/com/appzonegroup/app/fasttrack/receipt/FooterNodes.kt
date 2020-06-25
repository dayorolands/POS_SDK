package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.R
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 06/10/2019.
 * Appzone Ltd
 */

fun footerNodes(context: Context) = listOf<PrintNode>(

    TextNode("-----------------------------").apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode(
        "${context.getString(R.string.app_name)} v${BuildConfig.VERSION_NAME}. Powered by ${context.getString(
            R.string.institution_name
        )}"
    ).apply {
        align = Alignment.MIDDLE
        wordFont = 15
    },

    TextNode(context.getString(R.string.institution_website))
        .apply {
        align = Alignment.MIDDLE
        walkPaperAfterPrint = 10
        wordFont = 15
    }
)