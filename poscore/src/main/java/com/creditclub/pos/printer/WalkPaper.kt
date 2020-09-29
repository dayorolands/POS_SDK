package com.creditclub.pos.printer


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */

class WalkPaper(walk: Int) : PrintNode {
    override var walkPaperAfterPrint: Int = walk
}