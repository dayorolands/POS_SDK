package com.cluster.pos.card

import com.cluster.pos.models.messaging.BaseIsoMsg
import com.cluster.pos.PosParameter

open class CardIsoMsg : BaseIsoMsg() {

    var messageReasonCode56: String?
        get() = getString(56)
        set(value) = set(56, value)

    var transactionAmount4: String?
        get() = getString(4)
        set(value) = set(4, value)

    open fun apply(data: CardData) = applyCardData(data)

    fun withParameters(params: PosParameter.ManagementData) = applyManagementData(params)
}