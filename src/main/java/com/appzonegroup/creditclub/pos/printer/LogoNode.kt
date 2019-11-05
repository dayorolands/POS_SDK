package com.appzonegroup.creditclub.pos.printer

import com.appzonegroup.creditclub.pos.R


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
class LogoNode : ImageNode(R.drawable.cc_printer_logo) {
    override var walkPaperAfterPrint: Int = 20
}