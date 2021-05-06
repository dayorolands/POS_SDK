package com.creditclub.pos.printer

import androidx.annotation.DrawableRes

sealed class PrintNode {
    abstract var walkPaperAfterPrint: Int
}

data class ImageNode(
    @DrawableRes val drawable: Int,
    override var walkPaperAfterPrint: Int = 20,
    var align: Alignment = Alignment.MIDDLE,
    var printGray: Int = 5,
) : PrintNode()

data class TextNode(
    val text: String,
    var leftDistance: Int = 0,
    var lineDistance: Int = 0,
    var wordFont: Int = 22,
    var printGray: Int = 5,
    var isBold: Boolean = false,
    var align: Alignment = Alignment.LEFT,
    override var walkPaperAfterPrint: Int = 0,
) : PrintNode()

data class WalkPaper(override var walkPaperAfterPrint: Int = 20) : PrintNode()

enum class Alignment(val code: Int) {
    LEFT(0),
    MIDDLE(1),
    RIGHT(2)
}

data class BarCodeNode(
    val text: String,
    var leftDistance: Int = 0,
    var lineDistance: Int = 0,
    var wordFont: Int = 22,
    var printGray: Int = 5,
    var isBold: Boolean = false,
    var align: Alignment = Alignment.LEFT,
    override var walkPaperAfterPrint: Int = 0,
) : PrintNode()

enum class FontWeight {
    Small, Normal, Bold, Big
}

data class QrCodeNode(
    val text: String,
    var leftDistance: Int = 0,
    var lineDistance: Int = 0,
    var wordFont: Int = 22,
    var printGray: Int = 5,
    var isBold: Boolean = false,
    var align: Alignment = Alignment.LEFT,
    override var walkPaperAfterPrint: Int = 0,
) : PrintNode()
