package com.appzonegroup.creditclub.pos.models.messaging

open class NetworkManagement : BaseIsoMsg() {
    var managementDataOne: String?
        get() = getString(62)
        set(value) = set(62, value)

    var managementDataTwo: String?
        get() = getString(63)
        set(value) = set(63, value)

    init {
        mti = "0800"
    }

    override fun init() {
        super.init()
        mti = "0800"
    }
}
