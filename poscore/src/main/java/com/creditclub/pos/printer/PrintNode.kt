package com.creditclub.pos.printer

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

sealed class PrintNode : Parcelable {
    abstract var walkPaperAfterPrint: Int
}

@Parcelize
data class ImageNode(
    @DrawableRes val drawable: Int,
    override var walkPaperAfterPrint: Int = 20,
    var align: Alignment = Alignment.MIDDLE,
    var printGray: Int = 5,
) : PrintNode()

@Parcelize
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

@Parcelize
data class WalkPaper(override var walkPaperAfterPrint: Int = 20) : PrintNode()

enum class Alignment(val code: Int) {
    LEFT(0),
    MIDDLE(1),
    RIGHT(2)
}

@Parcelize
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

@Parcelize
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
