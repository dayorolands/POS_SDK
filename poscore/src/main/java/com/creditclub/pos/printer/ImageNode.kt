package com.creditclub.pos.printer

import androidx.annotation.DrawableRes


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
open class ImageNode(@DrawableRes val drawable: Int) :
    PrintNode {
    override var walkPaperAfterPrint: Int = 20
    var align: Alignment =
        Alignment.MIDDLE
    var printGray: Int = 5
}