package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.annotation.RawRes
import androidx.core.content.edit
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.util.*
import com.creditclub.core.CreditClubException
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.delegates.stringStore
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.model.ConnectionInfo
import com.creditclub.pos.utils.asDesEdeKey
import com.creditclub.pos.utils.decrypt
import com.creditclub.pos.utils.nonNullStringStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jpos.iso.ISOMsg
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.reflect.KProperty

class ParameterService(context: Context, val posMode: RemoteConnectionInfo) : PosParameter,
    KoinComponent {
    private val prefs: SharedPreferences = run {
        val suffix = "${posMode.ip}:${posMode.port}"
        val suffixHash = suffix.toByteArray().sha256String
        context.getEncryptedSharedPreferences("pos_parameters_0:$suffixHash")
    }
    private val config: PosConfig by inject()
    private val database: PosDatabase by inject()
    private val localStorage: LocalStorage by inject()
    private val resources = context.resources

    override var masterKey by prefs.nonNullStringStore("MasterKey")
    override var sessionKey by prefs.nonNullStringStore("SessionKey")
    override var pinKey by prefs.nonNullStringStore("PinKey")

    override var managementDataString by prefs.nonNullStringStore("PFMD", "{}")
    override var updatedAt by prefs.stringStore("UpdatedAt")
    override var capkList by ManagementDataDelegate("CAPK_ARRAY", R.raw.capk_data)
    override var emvAidList by ManagementDataDelegate("EMV_APP_ARRAY", R.raw.emv_app_data)

    override val managementData
        get() = safeRun {
            defaultJson.decodeFromString(ParameterObject.serializer(), managementDataString)
        }.data ?: ParameterObject()

    override suspend fun downloadKeys() {
        val terminalId = config.terminalId
        val masterKeyBytes = downloadKey(
            terminalId = terminalId,
            processingCode = "9A0000",
            decryptionKey = posMode.key1.hexBytes xor posMode.key2.hexBytes,
        )
        val sessionKeyBytes = downloadKey(
            terminalId = terminalId,
            processingCode = "9B0000",
            decryptionKey = masterKeyBytes,
        )
        val pinKeyBytes = downloadKey(
            terminalId = terminalId,
            processingCode = "9G0000",
            decryptionKey = masterKeyBytes,
        )

        pinKey = pinKeyBytes.hexString
        masterKey = masterKeyBytes.hexString
        sessionKey = sessionKeyBytes.hexString
        updatedAt = Instant.now().format("MMdd")
    }

    @Throws(ParameterDownloadException::class)
    override suspend fun downloadParameters() {
        val dateParams = TransmissionDateParams()

        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            set(3, "9C0000")
            set(7, dateParams.transmissionDateTime)
            val rrn = SecureRandom().nextInt(1000)
            val rrnString = String.format("%06d", rrn)
            set(11, rrnString)
            set(12, dateParams.localTime)
            set(13, dateParams.localDate)
            set(41, config.terminalId)
            set(62, "01008${config.terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.uppercase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = packedMsg + field64.toByteArray()

        debugOnly { isoMsg.log() }

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, finalMsgBytes)
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        debugOnly { isoMsg.log() }

        if (isoMsg.hasFailed) {
            debug("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw ParameterDownloadException(isoMsg.responseMessage)
        }
        val tlvString =
            isoMsg.getString(62) ?: throw ParameterDownloadException("No management data")
        managementDataString = parsePrivateFieldDataBlock(tlvString = tlvString).toString()
    }

    override suspend fun downloadCapk() {
        val dateParams = TransmissionDateParams()

        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            set(3, "9E0000")
            set(7, dateParams.transmissionDateTime)
            val rrn = SecureRandom().nextInt(1000)
            val rrnString = String.format("%06d", rrn)
            set(11, rrnString)
            set(12, dateParams.localTime)
            set(13, dateParams.localDate)
            set(41, config.terminalId)
            set(63, "01008${config.terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.uppercase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = packedMsg + field64.toByteArray()

        debugOnly { isoMsg.log() }

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, finalMsgBytes)
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        debugOnly { isoMsg.log() }

        if (isoMsg.hasFailed) {
            debug("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw PublicKeyDownloadException(isoMsg.responseMessage)
        }

        val capk = isoMsg.managementDataTwo63?.run {
            if (contains("~")) parsePrivateFieldData()
            else parseCapkData("${this}1")
        }

        capkList = capk ?: throw PublicKeyDownloadException("")
    }

    @Throws(EmvAidDownloadException::class)
    override suspend fun downloadAid() {
        val dateParams = TransmissionDateParams()

        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            set(3, "9F0000")
            set(7, dateParams.transmissionDateTime)
            val rrn = SecureRandom().nextInt(1000)
            val rrnString = String.format("%06d", rrn)
            set(11, rrnString)
            set(12, dateParams.localTime)
            set(13, dateParams.localDate)
            set(41, config.terminalId)
            set(63, "01008${config.terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.uppercase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = packedMsg + field64.toByteArray()
        debugOnly { isoMsg.log() }

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, finalMsgBytes)
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        debugOnly { isoMsg.log() }

        if (isoMsg.hasFailed) {
            debug("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw EmvAidDownloadException(isoMsg.responseMessage)
        }

        val aids = isoMsg.managementDataTwo63?.parsePrivateFieldData()
        emvAidList = aids ?: throw EmvAidDownloadException("")
    }

    override fun reset() {
        masterKey = ""
        sessionKey = ""
        pinKey = ""
        updatedAt = ""
    }

    private fun ISOMsg.generateRequestLog(): IsoRequestLog {

        return generateLog().apply {
            institutionCode = localStorage.institutionCode ?: ""
            agentCode = localStorage.agent?.agentCode ?: ""
            gpsCoordinates = localStorage.lastKnownLocation ?: "0.00;0.00"
            nodeName = posMode.nodeName
            if (posMode is ConnectionInfo) {
                connectionInfo = posMode
            }
        }
    }

    private fun IsoRequestLog.saveToDb(serverResponseCode: String) {
        responseTime = Instant.now()
        responseCode = serverResponseCode

        Looper.myLooper() ?: Looper.prepare()
        val dao = database.isoRequestLogDao()
        dao.save(this)
    }

    @Throws(JSONException::class)
    private fun String.parsePrivateFieldData(): JSONArray {
        val jsonArray = JSONArray()
        for (tlvString in split("~")) {
            jsonArray.put(parsePrivateFieldDataBlock(tlvString))
        }
        return jsonArray
    }

    @Throws(JSONException::class)
    private fun parsePrivateFieldDataBlock(tlvString: String): JSONObject {
        var index = 0
        val jsonObject = JSONObject()

        while (index < tlvString.length) {
            val tag = tlvString.substring(index, index + 2)
            index += 2
            val tagLength = tlvString.substring(index, index + 3).toInt()
            index += 3
            val tagData = tlvString.substring(index, index + tagLength)
            index += tagLength
            jsonObject.put(tag, tagData)
        }

        return jsonObject
    }

    private fun parseCapkData(tlvString: String): JSONArray {
        val jsonArray = JSONArray()
        val strLength = tlvString.length
        var index = 0
        while (index < strLength) {
            val jsonObject = JSONObject()
            while (true) {
                val tag = tlvString.substring(index, index + 2)
                index += 2
                val tagLength = tlvString.substring(index, index + 3).toInt()
                index += 3
                val tagData = tlvString.substring(index, index + tagLength)
                index += tagLength

                jsonObject.put(tag, tagData)

                if (tag == "40") break
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    class ParameterDownloadException(message: String) :
        CreditClubException("Parameter Download Failed. $message")

    class PublicKeyDownloadException(message: String) :
        CreditClubException("CA Public Key Download Failed. $message")

    class EmvAidDownloadException(message: String) :
        CreditClubException("EMV Application AID Download Failed. $message")

    private suspend fun downloadKey(
        terminalId: String,
        processingCode: String,
        decryptionKey: ByteArray
    ): ByteArray {
        val dateParams = TransmissionDateParams()
        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            processingCode3 = processingCode
            transmissionDateTime7 = dateParams.transmissionDateTime
            stan11 = String.format("%06d", SecureRandom().nextInt(1000))
            localTransactionTime12 = dateParams.localTime
            localTransactionDate13 = dateParams.localDate
            terminalId41 = terminalId
        }
        debugOnly { isoMsg.log() }
        val (output, error) = safeRunIO { SocketJob.execute(posMode, isoMsg.pack()) }
        val isoRequestLog = isoMsg.generateRequestLog()
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            throw error ?: IOException("No response")
        }
        isoMsg.unpack(output)
        withContext(Dispatchers.IO) {
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        debugOnly { isoMsg.log() }
        if (isoMsg.hasFailed) {
            throw CreditClubException(
                """
                    |Key Download failed
                    |Error contacting ${posMode.label}. 
                    |Server ${posMode.ip}:${posMode.port}. 
                    |${isoMsg.responseMessage}
                    |""".trimMargin()
            )
        }
        val cryptKey = decryptionKey.asDesEdeKey
        val cryptData = isoMsg.getString(53).substring(0, 32).hexBytes

        return cryptKey.decrypt(cryptData).copyOf(16)
    }

    @Serializable
    data class ParameterObject(
        @SerialName("03")
        override val cardAcceptorId: String = "",

        @SerialName("05")
        override val currencyCode: String = "",

        @SerialName("06")
        override val countryCode: String = "",

        @SerialName("08")
        override val merchantCategoryCode: String = "",

        @SerialName("52")
        override val cardAcceptorLocation: String = "",
    ) : PosParameter.ManagementData

    private inner class ManagementDataDelegate(
        private val key: String,
        @RawRes private val fallbackFileLocation: Int
    ) {
        private var jsonArray: JSONArray? = null
        operator fun getValue(obj: Any, prop: KProperty<*>): JSONArray? {
            if (jsonArray != null) return jsonArray
            val value =
                prefs.getString(key, null) ?: resources.readRawFileText(fallbackFileLocation)
            jsonArray = JSONArray(value)
            return jsonArray
        }

        operator fun setValue(obj: Any, prop: KProperty<*>, newValue: JSONArray?) {
            jsonArray = newValue
            prefs.edit {
                putString(key, newValue?.toString())
            }
        }
    }
}