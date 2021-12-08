package com.cluster.pos.printer

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

sealed interface PrintNode : Parcelable {
    val walkPaperAfterPrint: Int
    val align: Alignment
}

@Parcelize
data class ImageNode(
    @DrawableRes val drawable: Int,
    override var walkPaperAfterPrint: Int = 20,
    override val align: Alignment = Alignment.MIDDLE,
    val printGray: Int = 5,
) : PrintNode

@Parcelize
data class TextNode(
    val text: String,
    val leftDistance: Int = 0,
    val lineDistance: Int = 0,
    var wordFont: Int = 22,
    val printGray: Int = 5,
    val isBold: Boolean = false,
    override var align: Alignment = Alignment.LEFT,
    override val walkPaperAfterPrint: Int = 0,
) : PrintNode

@Parcelize
data class WalkPaper(
    override val walkPaperAfterPrint: Int = 20,
    override var align: Alignment = Alignment.LEFT,
) : PrintNode

enum class Alignment {
    LEFT,
    MIDDLE,
    RIGHT,
}

enum class FontWeight {
    Small, Normal, Bold, Big
}
