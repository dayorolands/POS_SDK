package com.globalaccelerex.nexgo.n3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import com.creditclub.core.util.delegates.jsonArrayStore
import com.creditclub.core.util.delegates.stringStore
import com.creditclub.core.util.format
import com.creditclub.pos.PosParameter
import com.creditclub.pos.utils.nonNullStringStore
import com.globalaccelerex.nexgo.n3.models.ParameterObject
import com.globalaccelerex.nexgo.n3.models.ParameterResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONArray
import org.koin.core.KoinComponent
import java.time.Instant

class N3ParameterStore constructor(context: Context) : PosParameter, KoinComponent {
    private val prefs: SharedPreferences = context.getSharedPreferences("N3Parameters", 0)

    override var masterKey: String by prefs.nonNullStringStore("MasterKey")
    override var sessionKey: String by prefs.nonNullStringStore("SessionKey")
    override var pinKey: String by prefs.nonNullStringStore("PinKey")
    private val json: Json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )

    override val managementData: PosParameter.ManagementData
        get() = json.parse(ParameterObject.serializer(), managementDataString)

    override var managementDataString: String by prefs.nonNullStringStore("PFMD", "{}")
    override var updatedAt by prefs.stringStore("UpdatedAt")

    override var capkList by prefs.jsonArrayStore("CAPK_ARRAY")
    override var emvAidList by prefs.jsonArrayStore("EMV_APP_ARRAY")

    override suspend fun downloadKeys(activity: ComponentActivity) {
        val request = Intent("com.globalaccelerex.keyexchange")
        val result = activity.getActivityResult(request)
        val status = result.data?.getStringExtra("status")
        status ?: throw IllegalArgumentException("Status cannot be null")
        if (status != "00") throw RuntimeException(getResponseMessage(status))

        masterKey = "placeholder"
        sessionKey = "placeholder"
        pinKey = "placeholder"
        updatedAt = Instant.now().format("MMdd")
    }

    override suspend fun downloadParameters(activity: ComponentActivity) {
        managementDataString = """
            {
                "MerchantID": "3169MB126086441",
                "TerminalID": "3181BM41",
                "serialNumber": "N5000011238",
                "PTSP": "Global Accelerex",
                "FooterMessage": "THANKS FOR COMING",
                "State": "02",
                "MerchantName": "KBP GLOBAL SERVICES ",
                "BankName": "GT BANK",
                "City": "Lagos"
            }
        """.trimIndent()
        capkList = JSONArray()
        emvAidList = JSONArray()

        val jsonString = "{ \"action\":\"PARAMETER\" }"
        val request = Intent("com.globalaccelerex.utility")
        request.putExtra("requestData", jsonString)
        val result = activity.getActivityResult(request)
        val status = result.data?.getStringExtra("status")
        status ?: throw IllegalArgumentException("Status should be null")
        if (status != "00") throw RuntimeException(getResponseMessage(status))
        val data = result.data?.getStringExtra("data")
        data ?: throw IllegalArgumentException("Data should be null")

        val parameterResponse = json.parse(
            ParameterResponse.serializer(),
            data
        )
        managementDataString = json.stringify(
            ParameterObject.serializer(),
            ParameterObject().apply {
                cardAcceptorId = parameterResponse.merchantId ?: ""
                cardAcceptorLocation = parameterResponse.city ?: ""
            }
        )
    }

    override suspend fun downloadCapk(activity: ComponentActivity) {

    }

    override suspend fun downloadAid(activity: ComponentActivity) {

    }

    override fun reset() {
        prefs.edit().clear().apply()
    }
}