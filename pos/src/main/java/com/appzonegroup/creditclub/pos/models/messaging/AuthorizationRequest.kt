package com.appzonegroup.creditclub.pos.models.messaging

import com.appzonegroup.creditclub.pos.card.CardIsoMsg

open class AuthorizationRequest : CardIsoMsg() {

    override fun init() {
        super.init()
        mti = "0100"
    }
}
