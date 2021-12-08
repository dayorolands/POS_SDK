package com.cluster.pos

import android.content.Context
import com.cluster.pos.service.ParameterService
import com.cluster.core.util.readRawFileText
import org.json.JSONArray

class DummyPosParameter(context: Context) : PosParameter {
    override var masterKey: String = "07167611FA61C01B867820CD90603BF3"
    override var sessionKey: String = "B770FE24BF38B33CF568A7F1D9006602"
    override var pinKey: String = "DC538C2563F3D0DC3C766E15545D445F"
    override var updatedAt: String? = ""
    override var managementDataString: String = "{}"
    override val managementData: PosParameter.ManagementData = defaultManagementData
    override val capkList: JSONArray = JSONArray(context.resources.readRawFileText(R.raw.capk_data))
    override val emvAidList: JSONArray =
        JSONArray(context.resources.readRawFileText(R.raw.emv_app_data))

    override fun reset() {}
    override suspend fun downloadCapk() {}
    override suspend fun downloadAid() {}
    override suspend fun downloadParameters() {}
    override suspend fun downloadKeys() {}
}

val defaultManagementData = object : PosParameter.ManagementData {
    override val cardAcceptorId: String = ""
    override val currencyCode: String = ""
    override val countryCode: String = ""
    override val merchantCategoryCode: String = ""
    override val cardAcceptorLocation: String = ""
}

fun RemoteConnectionInfo.getParameter(context: Context): PosParameter {
    if (this == InvalidRemoteConnectionInfo) return DummyPosParameter(context)

    return ParameterService(context, this)
}