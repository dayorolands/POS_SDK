package com.appzonegroup.creditclub.pos.models.messaging

import com.appzonegroup.creditclub.pos.card.CardIsoMsg

open class AuthorizationRequest : CardIsoMsg() {
    var managementDataTwo: String?
        get() = getString(63)
        set(value) = set(63, value)

    override fun init() {
        super.init()
        mti = "0100"
    }
}
