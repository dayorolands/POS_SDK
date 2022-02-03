package com.cluster.pos.models.messaging

import com.cluster.pos.card.CardIsoMsg

open class AuthorizationRequest : CardIsoMsg() {

    override fun init() {
        super.init()
        mti = "0100"
    }
}
