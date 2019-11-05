package com.appzonegroup.creditclub.pos.service

import android.content.Context
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.contract.DialogProvider
import com.appzonegroup.creditclub.pos.extension.hasFailed
import com.appzonegroup.creditclub.pos.extension.responseMessage
import com.appzonegroup.creditclub.pos.util.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
open class ParameterService protected constructor(context: Context) {
    private val prefs by lazy { context.getSharedPreferences("Parameters", 0) }
    internal open val config by lazy {
        ConfigService.getInstance(context)
    }

    open var masterKey = prefs.getString("MasterKey", "") as String
        set(value) {
            field = value
            prefs.edit().putString("MasterKey", value).apply()
        }

    open var sessionKey = prefs.getString("SessionKey", "") as String
        set(value) {
            field = value
            prefs.edit().putString("SessionKey", value).apply()
        }

    open var pinKey = prefs.getString("PinKey", "") as String
        set(value) {
            field = value
            prefs.edit().putString("PinKey", value).apply()
        }

    open var pfmd = prefs.getString("PFMD", "{}") as String
        set(value) {
            field = value
            prefs.edit().putString("PFMD", value).apply()
        }

    open var updatedAt = prefs.getString("UpdatedAt", "") as String
        set(value) {
            field = value
            prefs.edit().putString("UpdatedAt", value).apply()
        }

    val parameters: ParameterObject
        get() = try {
            Gson().fromJson(pfmd, ParameterObject::class.java)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ParameterObject()
        }

    fun downloadAsync(dialogProvider: DialogProvider? = null, force: Boolean = false) {
        val localDate = TransmissionDateParams().localDate
        if (updatedAt == localDate && !force) return

        GlobalScope.launch(Dispatchers.Main) {
            dialogProvider?.showProgressBar("Downloading Keys and Parameters")

            try {
                masterKey = withContext(Dispatchers.Default) {
                    downloadMasterKey()
                }

                sessionKey = withContext(Dispatchers.Default) {
                    downloadSessionKey()
                }

                pinKey = withContext(Dispatchers.Default) {
                    downloadPinKey()
                }

                pfmd = withContext(Dispatchers.Default) {
                    downloadParameters()
                }

                updatedAt = localDate

                dialogProvider?.hideProgressBar()
                dialogProvider?.showSuccess("Download successful")
            } catch (ex: Exception) {
                dialogProvider?.hideProgressBar()
                dialogProvider?.showError("Download Failed. ${ex.message}")
            }
        }
    }

    fun downloadKeysAsync(dialogProvider: DialogProvider? = null, force: Boolean = false) {
        val localDate = TransmissionDateParams().localDate
        if (updatedAt == localDate && !force) return

        GlobalScope.launch(Dispatchers.Main) {
            dialogProvider?.showProgressBar("Downloading Keys")
            try {
                masterKey = withContext(Dispatchers.Default) {
                    downloadMasterKey()
                }

                sessionKey = withContext(Dispatchers.Default) {
                    downloadSessionKey()
                }

                pinKey = withContext(Dispatchers.Default) {
                    downloadPinKey()
                }

                updatedAt = localDate

                dialogProvider?.hideProgressBar()
                dialogProvider?.showSuccess("Download successful")
            } catch (ex: Exception) {
                masterKey = ""
                sessionKey = ""
                pinKey = ""
                updatedAt = ""

                dialogProvider?.hideProgressBar()
                dialogProvider?.showError(ex.message ?: "Key Download Failed.")
            }
        }
    }

    @Throws(KeyDownloadException::class)
    fun downloadMasterKey(): String {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9A0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = Random().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)
        isoMsg.packager = packager

        TerminalUtils.logISOMsg(isoMsg)
        val output = SocketJob.sslSocketConnectionJob(config.ip, config.port, isoMsg.pack())

        println("MESSAGE: " + String(output!!))
        isoMsg.unpack(output)

        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val posMode = config.posMode
        val cryptKey = ISOUtil.xor(Misc.toByteArray(posMode.key1), Misc.toByteArray(posMode.key2))
        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        return TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
    }

    @Throws(KeyDownloadException::class)
    fun downloadSessionKey(): String {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9B0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = Random().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)


        isoMsg.packager = packager

        TerminalUtils.logISOMsg(isoMsg)

        val output = SocketJob.sslSocketConnectionJob(config.ip, config.port, isoMsg.pack())

        println("MESSAGE: " + String(output!!))
        isoMsg.unpack(output)
        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }
        val cryptKey = TerminalUtils.hexStringToByteArray(masterKey)

        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        return TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
    }

    @Throws(KeyDownloadException::class)
    fun downloadPinKey(): String {
        val dateParams = TransmissionDateParams()
        val packager = ISO87Packager()

        val isoMsg = ISOMsg()
        isoMsg.mti = "0800"
        isoMsg.set(3, "9G0000")
        isoMsg.set(7, dateParams.transmissionDateTime)
        val rrn = Random().nextInt(1000)
        val rrnString = String.format("%06d", rrn)
        isoMsg.set(11, rrnString)
        isoMsg.set(12, dateParams.localTime)
        isoMsg.set(13, dateParams.localDate)
        isoMsg.set(41, config.terminalId)


        isoMsg.packager = packager

        TerminalUtils.logISOMsg(isoMsg)

        val output = SocketJob.sslSocketConnectionJob(config.ip, config.port, isoMsg.pack())

        println("MESSAGE: " + String(output!!))
        isoMsg.unpack(output)

        TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            println("Error contacting Nibss server")
            throw KeyDownloadException(isoMsg.responseMessage)
        }

        val cryptKey = TerminalUtils.hexStringToByteArray(masterKey)
        val cryptData = TerminalUtils.hexStringToByteArray(isoMsg.getString(53).substring(0, 32))

        val tripleDesCipher = TripleDesCipher(cryptKey)
        return TerminalUtils.byteArrayToHex(tripleDesCipher.decrypt(cryptData).copyOf(16))
    }

    @Throws(ParameterDownloadException::class)
    fun downloadParameters(): String {
        val dateParams = TransmissionDateParams()

        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            set(3, "9C0000")
            set(7, dateParams.transmissionDateTime)
            val rrn = Random().nextInt(1000)
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

        val output = SocketJob.sslSocketConnectionJob(config.ip, config.port, finalMsgBytes)
        isoMsg.unpack(output)

        if (BuildConfig.DEBUG) TerminalUtils.logISOMsg(isoMsg)

        if (isoMsg.hasFailed) {
            if (BuildConfig.DEBUG) println("Error contacting Nibss server")
            throw ParameterDownloadException(isoMsg.responseMessage)
        }

        println("Secured connection performed successfully")

        return TerminalUtils.parsePrivateFieldData(isoMsg.getString(62))
            ?: throw ParameterDownloadException("")
    }


    fun downloadParametersAsync(dialogProvider: DialogProvider) {
        GlobalScope.launch(Dispatchers.Main) {
            dialogProvider.showProgressBar("Downloading Parameters")
            try {
                pfmd = withContext(Dispatchers.Default) {
                    downloadParameters()
                }

                dialogProvider.hideProgressBar()
                dialogProvider.showSuccess("Download successful")
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
                dialogProvider.hideProgressBar()
                dialogProvider.showError(ex.message ?: "Parameter Download Failed")
            }
        }
    }

    fun reset() {
        masterKey = ""
        sessionKey = ""
        pinKey = ""
        updatedAt = ""
    }

    class KeyDownloadException(message: String) : Exception("Key Download Failed. $message")

    class ParameterDownloadException(message: String) :
        Exception("Parameter Download Failed. $message")

    class ParameterObject {
        @SerializedName("03")
        var cardAcceptorId = ""

        @SerializedName("05")
        var currencyCode = ""

        @SerializedName("06")
        var countryCode = ""

        @SerializedName("08")
        var merchantCategoryCode = ""

        @SerializedName("52")
        var cardAcceptorLocation = ""
    }

    companion object {
        private var INSTANCE: ParameterService? = null

        fun getInstance(context: Context): ParameterService {
            if (INSTANCE == null) INSTANCE =
                ParameterService(context.applicationContext)
            return INSTANCE as ParameterService
        }
    }
}