package com.appzonegroup.creditclub.pos.printer

import android.content.Context
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.R
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode


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