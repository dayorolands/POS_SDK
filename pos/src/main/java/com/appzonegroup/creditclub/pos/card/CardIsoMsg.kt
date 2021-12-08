package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardData

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