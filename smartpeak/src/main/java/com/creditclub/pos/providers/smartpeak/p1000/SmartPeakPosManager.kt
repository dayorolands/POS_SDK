package com.creditclub.pos.providers.smartpeak.p1000

import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.PosManager

class SmartPeakPosManager : PosManager {
    override val cardReader: CardReader
        get() = TODO("Not yet implemented")

    override val sessionData: PosManager.SessionData
        get() = TODO("Not yet implemented")

    override fun loadEmv() {
        TODO("Not yet implemented")
    }

    override fun cleanUpEmv() {
        TODO("Not yet implemented")
    }
}