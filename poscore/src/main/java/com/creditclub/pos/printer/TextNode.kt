package com.creditclub.pos.printer


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
open class TextNode(open val text: String) :
    PrintNode {
    var leftDistance: Int = 0
    var lineDistance: Int = 0
    var wordFont: Int = 22
    var printGray: Int = 5
    var align: Alignment =
        Alignment.LEFT
    override var walkPaperAfterPrint: Int = 0
}