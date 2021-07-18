package com.appzonegroup.creditclub.pos.service

import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import org.json.JSONArray

val defaultManagementData = object : PosParameter.ManagementData {
    override val cardAcceptorId: String = ""
    override val currencyCode: String = ""
    override val countryCode: String = ""
    override val merchantCategoryCode: String = ""
    override val cardAcceptorLocation: String = ""
}