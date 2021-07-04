package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.util.ISO87Packager
import com.appzonegroup.creditclub.pos.util.TransmissionDateParams
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import org.jpos.iso.ISOException
import org.jpos.transaction.TransactionManager
import org.json.JSONException
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

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