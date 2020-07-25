package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.activity.ComponentActivity
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.util.*
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.delegates.jsonArrayStore
import com.creditclub.core.util.delegates.stringStore
import com.creditclub.core.util.format
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.utils.nonNullStringStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.Instant
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
class ParameterService(context: Context, posMode: RemoteConnectionInfo? = null) : PosParameter,
    KoinComponent {
    private val prefs: SharedPreferences = run {
        val route = posMode?.id ?: "Default"
        context.getSharedPreferences("Parameters~${route}", 0)
    }
    private val config: PosConfig by inject()
    private val database: PosDatabase by inject()
    private val localStorage: LocalStorage by inject()
    private val gps: TrackGPS by inject()
    private val json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )

    override var masterKey by prefs.nonNullStringStore("MasterKey")
    override var sessionKey by prefs.nonNullStringStore("SessionKey")
    override var pinKey by prefs.nonNullStringStore("PinKey")

    override var managementDataString by prefs.nonNullStringStore("PFMD", "{}")
    override var updatedAt by prefs.stringStore("UpdatedAt")
    override var capkList by prefs.jsonArrayStore("CAPK_ARRAY")
    override var emvAidList by prefs.jsonArrayStore("EMV_APP_ARRAY")

    override val managementData
        get() = safeRun {
            json.parse(ParameterObject.serializer(), managementDataString)
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

        TerminalUtils.logISOMsg(isoMsg)
        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                isoMsg.pack()
            )
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

        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val posMode = config.remoteConnectionInfo
        val cryptKey = ISOUtil.xor(Misc.toByteArray(posMode.key1), Misc.toByteArray(posMode.key2))
        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        masterKey = TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
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

        TerminalUtils.logISOMsg(isoMsg)

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                isoMsg.pack()
            )
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
        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val cryptKey = TerminalUtils.hexStringToByteArray(masterKey)

        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        sessionKey = TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
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

        TerminalUtils.logISOMsg(isoMsg)

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                isoMsg.pack()
            )
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

        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }

        val cryptKey = TerminalUtils.hexStringToByteArray(masterKey)
        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        pinKey = TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
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

        val baos = ByteArrayOutputStream().apply {
            write(TerminalUtils.hexStringToByteArray(sessionKey))
            write(packedMsg)
        }

        val field64 = TerminalUtils.sha256(baos.toByteArray()).toUpperCase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = TerminalUtils.constructField64_128(packedMsg, field64.toByteArray())

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                finalMsgBytes
            )
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            if (BuildConfig.DEBUG) println("Error contacting Nibss server")
            throw ParameterDownloadException(isoMsg.responseMessage)
        }

        println("Secured connection performed successfully")

        managementDataString = TerminalUtils.parsePrivateFieldData(isoMsg.getString(62))
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

        val baos = ByteArrayOutputStream().apply {
            write(TerminalUtils.hexStringToByteArray(sessionKey))
            write(packedMsg)
        }

        val field64 = TerminalUtils.sha256(baos.toByteArray()).toUpperCase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = TerminalUtils.constructField64_128(packedMsg, field64.toByteArray())

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                finalMsgBytes
            )
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            if (BuildConfig.DEBUG) println("Error contacting Nibss server")
            throw PublicKeyDownloadException(isoMsg.responseMessage)
        }

        val capk = isoMsg.managementDataTwo63?.parsePrivateFieldData()

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

        val baos = ByteArrayOutputStream().apply {
            write(TerminalUtils.hexStringToByteArray(sessionKey))
            write(packedMsg)
        }

        val field64 = TerminalUtils.sha256(baos.toByteArray()).toUpperCase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = TerminalUtils.constructField64_128(packedMsg, field64.toByteArray())

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        val isoRequestLog = isoMsg.generateRequestLog()
        val (output, error) = safeRun {
            SocketJob.sslSocketConnectionJob(
                config.remoteConnectionInfo.ip,
                config.remoteConnectionInfo.port,
                finalMsgBytes
            )
        }
        if (output == null) {
            isoRequestLog.saveToDb("TE")
            if (error != null) throw error
        } else {
            isoMsg.unpack(output)
            isoRequestLog.saveToDb(isoMsg.responseCode39 ?: "XX")
        }

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            if (BuildConfig.DEBUG) println("Error contacting Nibss server")
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