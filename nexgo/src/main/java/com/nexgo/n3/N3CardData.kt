package com.nexgo.n3

import com.creditclub.pos.card.CardData

class N3CardData : CardData() {
    override var track2 = ""
        set(value) {
            field = if (value.length > 37) {
                value.substring(0, 37)
            } else value
        }

    internal var mIccString = ""

    override val iccString: String get() = mIccString
}