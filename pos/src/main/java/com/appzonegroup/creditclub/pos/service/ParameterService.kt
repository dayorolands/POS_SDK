package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.activity.ComponentActivity
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.util.*
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.jsonArrayStore
import com.creditclub.core.util.delegates.stringStore
import com.creditclub.core.util.format
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.model.ConnectionInfo
import com.creditclub.pos.utils.TripleDesCipher
import com.creditclub.pos.utils.nonNullStringStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jpos.iso.ISOMsg
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.security.SecureRandom
import java.time.Instant
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
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
    private val gps: TrackGPS by inject()
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
        encodeDefaults = true
    }

    override var masterKey by prefs.nonNullStringStore("MasterKey")
    override var sessionKey by prefs.nonNullStringStore("SessionKey")
    override var pinKey by prefs.nonNullStringStore("PinKey")

    override var managementDataString by prefs.nonNullStringStore("PFMD", "{}")
    override var updatedAt by prefs.stringStore("UpdatedAt")
    override var capkList by prefs.jsonArrayStore("CAPK_ARRAY")
    override var emvAidList by prefs.jsonArrayStore("EMV_APP_ARRAY")

    override val managementData
        get() = safeRun {
            json.decodeFromString(ParameterObject.serializer(), managementDataString)
        }.data ?: ParameterObject()

    override suspend fun downloadKeys(activity: ComponentActivity) {
        withContext(Dispatchers.Default) {
            downloadMasterKey()
            downloadSessionKey()
            downloadPinKey()
        }

        updatedAt = Instant.now().format("MMdd")
    }

    @Throws(KeyDownloadException::class)
    private fun downloadMasterKey() {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9A0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = SecureRandom().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)
        isoMsg.packager = packager

        isoMsg.log()
        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, isoMsg.pack())
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            throw error!!
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        println("MESSAGE: " + String(output))
        isoMsg.unpack(output)

        isoMsg.log()

        if (isoMsg.hasFailed) {
            println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val cryptKey = posMode.key1.hexBytes xor posMode.key2.hexBytes
        val cryptData = isoMsg.getString(53).substring(0, 32).hexBytes

        val tripleDesCipher = TripleDesCipher(cryptKey)
        masterKey = tripleDesCipher.decrypt(cryptData).copyOf(16).hexString
    }

    @Throws(KeyDownloadException::class)
    private fun downloadSessionKey() {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9B0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = SecureRandom().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)


        isoMsg.packager = packager

        isoMsg.log()

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, isoMsg.pack())
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            throw error!!
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        println("MESSAGE: " + String(output))
        isoMsg.unpack(output)
        isoMsg.log()

        if (isoMsg.hasFailed) {
            println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val cryptKey = masterKey.hexBytes
        val cryptData = isoMsg.getString(53).substring(0, 32).hexBytes

        val tripleDesCipher = TripleDesCipher(cryptKey)
        sessionKey = tripleDesCipher.decrypt(cryptData).copyOf(16).hexString
    }

    @Throws(KeyDownloadException::class)
    private fun downloadPinKey() {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9G0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = SecureRandom().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)

        isoMsg.packager = packager

        isoMsg.log()

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.execute(posMode, isoMsg.pack())
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            throw error!!
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        println("MESSAGE: " + String(output))
        isoMsg.unpack(output)

        isoMsg.log()
        if (isoMsg.hasFailed) {
            println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            throw KeyDownloadException(isoMsg.responseMessage)
        }

        val cryptKey = masterKey.hexBytes
        val cryptData = isoMsg.getString(53).substring(0, 32).hexBytes

        val tripleDesCipher = TripleDesCipher(cryptKey)
        pinKey = tripleDesCipher.decrypt(cryptData).copyOf(16).hexString
    }

    @Throws(ParameterDownloadException::class)
    override suspend fun downloadParameters(activity: ComponentActivity) {
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
        val field64 = baos.sha256String.toUpperCase(Locale.getDefault())
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
            debugOnly { println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}") }
            throw ParameterDownloadException(isoMsg.responseMessage)
        }

        println("Secured connection performed successfully")

        managementDataString = isoMsg.getString(62)?.parsePrivateFieldDataBlock()?.toString()
            ?: throw ParameterDownloadException("")
    }

    override suspend fun downloadCapk(activity: ComponentActivity) {
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
            set(62, "01008${config.terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.toUpperCase(Locale.getDefault())
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
            debugOnly { println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}") }
            throw PublicKeyDownloadException(isoMsg.responseMessage)
        }

        val capk = isoMsg.managementDataTwo63?.run {
            if (contains("~")) parsePrivateFieldData()
            else "${this}1".parseCapkData()
        }

        capkList = capk ?: throw PublicKeyDownloadException("")
    }

    @Throws(EmvAidDownloadException::class)
    override suspend fun downloadAid(activity: ComponentActivity) {
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
            set(62, "01008${config.terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.toUpperCase(Locale.getDefault())
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
            debugOnly {
                println("Error contacting ${posMode.label} server ${posMode.ip}:${posMode.port}")
            }
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
            gpsCoordinates = gps.geolocationString ?: "0.00;0.00"
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
    private inline fun String.parsePrivateFieldData(): JSONArray {
        val jsonArray = JSONArray()
        for (s in split("~")) {
            jsonArray.put(s.parsePrivateFieldDataBlock())
        }
        return jsonArray
    }

    @Throws(JSONException::class)
    private inline fun String.parsePrivateFieldDataBlock(): JSONObject {
        var index = 0
        val jsonObject = JSONObject()

        while (index < length) {
            val tag = substring(index, index + 2)
            index += 2
            val tagLength = substring(index, index + 3).toInt()
            index += 3
            val tagData = substring(index, index + tagLength)
            index += tagLength
            jsonObject.put(tag, tagData)
        }

        return jsonObject
    }

    private fun String.parseCapkData(): JSONArray {
        val jsonArray = JSONArray()
        val strLength = length
        var index = 0
        while (index < strLength) {
            val jsonObject = JSONObject()
            while (true) {
                val tag = substring(index, index + 2)
                index += 2
                val tagLength = substring(index, index + 3).toInt()
                index += 3
                val tagData = substring(index, index + tagLength)
                index += tagLength

                jsonObject.put(tag, tagData)

                if (tag == "40") break
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    class KeyDownloadException(message: String) : Exception("Key Download Failed. $message")

    class ParameterDownloadException(message: String) :
        Exception("Parameter Download Failed. $message")

    class PublicKeyDownloadException(message: String) :
        Exception("CA Public Key Download Failed. $message")

    class EmvAidDownloadException(message: String) :
        Exception("EMV Application AID Download Failed. $message")

    @Serializable
    class ParameterObject : PosParameter.ManagementData {
        @SerialName("03")
        override var cardAcceptorId = ""

        @SerialName("05")
        override var currencyCode = ""

        @SerialName("06")
        override var countryCode = ""

        @SerialName("08")
        override var merchantCategoryCode = ""

        @SerialName("52")
        override var cardAcceptorLocation = ""
    }
}