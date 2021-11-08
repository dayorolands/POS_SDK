package com.appzonegroup.creditclub.pos

import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.*
import com.appzonegroup.creditclub.pos.util.xor
import com.creditclub.core.CreditClubException
import com.creditclub.core.util.debug
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.*
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.utils.asDesEdeKey
import com.creditclub.pos.utils.decrypt
import kotlinx.coroutines.runBlocking
import okio.use
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jpos.iso.ISOMsg
import org.jpos.iso.ISOUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.security.Security
import java.util.*
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

const val TERMINAL_ID = "2076KB84"
val posMode = object : RemoteConnectionInfo {
    override val id: String = "test"
    override val label: String = "test"
    override val key1: String = "3DFB3802940E8A546B3D38610852BA7A"
    override val key2: String = "0234E39861D3405E7A6B3185BA675873"
    override val host: String = "196.6.103.73"
    override val port: Int = 5043
    override val sslEnabled: Boolean = true
    override val dukptConfig: DukptConfig? = null
    override val timeout: Int = 60
    override val nodeName: String? = null
    override val requeryConfig: RequeryConfig? = null
}

class KeyDownloadTest : PosParameter {
    override var masterKey: String = ""
    override var sessionKey: String = ""
    override var pinKey: String = ""
    override var updatedAt: String? = ""
    override var managementDataString: String = ""
    override val managementData: PosParameter.ManagementData = defaultManagementData
    override val capkList: JSONArray? = null
    override val emvAidList: JSONArray? = null

    init {
        SocketJob.setTrustManagers(getTrustManagers())
    }

    override suspend fun downloadCapk() {
    }

    override suspend fun downloadAid() {
    }

    override suspend fun downloadParameters() {
    }

    @Test
    fun keys_downloadProperly() {
        runBlocking {
            downloadKeys()
            downloadParameters(terminalId = TERMINAL_ID)
        }
    }

    @Test
    fun message_unpacksProperly() {
        val messageStr = ""
        val packedMsg = messageStr.toByteArray()
        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
        }
        isoMsg.unpack(packedMsg)
        isoMsg.log()
    }

    @Throws(Exception::class)
    fun getTrustManagers(): Array<TrustManager?>? {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val password = "cluster".toCharArray()
        val trustStoreFile = File(javaClass.getResource("/pos_trust_store.bks").path)
        val trustStore = KeyStore.getInstance("BKS")
        trustStoreFile.inputStream().use { inputStream ->
            trustStore.load(inputStream, password)
        }
        val tmf = TrustManagerFactory.getInstance("X509")
        tmf.init(trustStore)
        return tmf.trustManagers
    }

    override suspend fun downloadKeys() {
        posMode.tcp().use { client ->
            val masterKeyBytes = client.downloadKey(
                processingCode = "9A0000",
                key = posMode.key1.hexBytes xor posMode.key2.hexBytes,
            )
            val sessionKeyBytes = client.downloadKey(
                processingCode = "9B0000",
                key = masterKeyBytes,
            )
            val pinKeyBytes = client.downloadKey(
                processingCode = "9G0000",
                key = masterKeyBytes,
            )

            masterKey = masterKeyBytes.hexString
            sessionKey = sessionKeyBytes.hexString
            pinKey = pinKeyBytes.hexString
        }

        assert(true)
    }

    private fun TcpClient.downloadKey(processingCode: String, key: ByteArray): ByteArray {
        val dateParams = TransmissionDateParams()
        val isoMsg = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0800"
            processingCode3 = processingCode
            transmissionDateTime7 = dateParams.transmissionDateTime
            stan11 = String.format("%06d", SecureRandom().nextInt(1000))
            localTransactionTime12 = dateParams.localTime
            localTransactionDate13 = dateParams.localDate
            terminalId41 = TERMINAL_ID
        }
        debugOnly { isoMsg.log() }
        val input = isoMsg.pack()
        val output = sendAndReceive(input)
        isoMsg.unpack(output)
        debugOnly { isoMsg.log() }

        if (isoMsg.hasFailed) {
            throw CreditClubException(
                """
                    |Key Download failed
                    |Error contacting ${posMode.label}. 
                    |Server ${posMode.host}:${posMode.port}. 
                    |${isoMsg.responseMessage}
                    |""".trimMargin()
            )
        }
        val cryptKey = key.asDesEdeKey
        val cryptData = isoMsg.getString(53).substring(0, 32).hexBytes

        return cryptKey.decrypt(cryptData).copyOf(16)
    }


    private fun downloadParameters(terminalId: String) {
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
            set(41, terminalId)
            set(62, "01008${terminalId}")
        }

        val packedMsg = isoMsg.pack()
        packedMsg[19]++

        val baos = sessionKey.hexBytes + packedMsg
        val field64 = baos.sha256String.uppercase(Locale.getDefault())
        isoMsg.set(64, field64)

        val finalMsgBytes = packedMsg + field64.toByteArray()

        debugOnly { isoMsg.log() }

        val output = SocketJob.execute(posMode, finalMsgBytes)
        isoMsg.unpack(output)

        debugOnly { isoMsg.log() }

        if (isoMsg.hasFailed) {
            debug("Error contacting ${posMode.label} server ${posMode.host}:${posMode.port}")
            throw ParameterService.ParameterDownloadException(isoMsg.responseMessage)
        }
        val tlvString =
            isoMsg.managementDataOne62
                ?: throw ParameterService.ParameterDownloadException("No management data")
        managementDataString = parsePrivateFieldDataBlock(tlvString = tlvString).toString()
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
}