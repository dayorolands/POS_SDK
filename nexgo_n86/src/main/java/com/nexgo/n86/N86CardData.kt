package com.nexgo.n86

import com.cluster.pos.card.CardData

class N86CardData : CardData() {
    override var track2 = ""
        set(value) {
            field = if (value.length > 37) {
                value.substring(0, 37)
            } else value
        }

    internal var mIccString = ""

    override val iccString: String get() = mIccString
}
