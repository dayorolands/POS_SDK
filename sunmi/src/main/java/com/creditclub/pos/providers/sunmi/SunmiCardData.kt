package com.creditclub.pos.providers.sunmi

import com.cluster.pos.card.CardData

internal class SunmiCardData : CardData() {
    override var track2 = ""
        set(value) {
            field = if (value.length > 37) {
                value.substring(0, 37)
            } else value
        }

    internal var mIccString = ""

    override val iccString: String get() = mIccString
}
