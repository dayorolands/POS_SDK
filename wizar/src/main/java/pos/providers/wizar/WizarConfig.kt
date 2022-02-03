package pos.providers.wizar

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.cloudpos.jniinterface.EMVJNIInterface
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.util.debug
import com.cluster.core.util.delegates.intStore
import com.cluster.core.util.delegates.stringStore
import com.cluster.pos.PosConfig
import com.cluster.pos.PosParameter
import com.cluster.pos.extensions.*
import com.wizarpos.emvsample.db.AIDTable
import com.wizarpos.emvsample.db.CAPKTable
import com.wizarpos.util.NumberUtil
import com.wizarpos.util.StringUtil

private inline val String.prependLength get() = "${length / 2}${this}"
private const val WIZAR_TERMINAL_CONFIG_FILE_NAME = "pos.providers.wizar.WizarConfig"
private const val DEFAULT_CONTACTLESS_LIMIT = 100000
private const val DEFAULT_CONTACTLESS_FLOOR_LIMIT = 100000
private const val DEFAULT_CONTACTLESS_CVM_LIMIT = 1000
private const val DEFAULT_TERMINAL_TYPE = "22"

internal fun loadAID(posParameter: PosParameter): Int {
    val aidJsonArray = posParameter.emvAidList ?: return -1
    EMVJNIInterface.emv_aidparam_clear()

    for (i in 0 until aidJsonArray.length()) {
        val jsonObject = aidJsonArray.getJSONObject(i)
        val aidTable = AIDTable().apply {
            //                AppName = jsonObject.appName17.toByteArray(StandardCharsets.US_ASCII)
            aid = jsonObject.aid15
//                SelFlag = 0
//                Priority = jsonObject.selectionPriority19.hexByte
            targetPercentage = jsonObject.targetPercentageDomestic27.hexByte
            maxTargetPercentage = jsonObject.maxTargetDomestic25.hexByte
//                FloorLimitCheck = 1
//                RandTransSel = 1
//                VelocityCheck = 1
//                floorLimit = jsonObject.tflDomestic22
//                threshold = jsonObject.offlineThresholdDomestic24
            tacDenial = jsonObject.tacDenial30
            tacOnline = jsonObject.tacOnline31
            tacDefault = jsonObject.defaultTacValue29
//                AcquierId = byteArrayOf(1, 35, 69, 103, -119, 16)
            defaultDDOL = jsonObject.ddol20.prependLength
//                TDOL = jsonObject.tdol21.prependLength.hexBytes
            appVersionNumber = jsonObject.appVersion18
//                RiskManData =
//                    byteArrayOf(0x6C, 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            supportOnlinePin = 1
        }
        val dataBuffer = aidTable.dataBuffer
        val ret = EMVJNIInterface.emv_aidparam_add(dataBuffer, dataBuffer.size)
        if (ret < 0) {
            debug("Failed to load aid ${aidTable.aid + "_" + aidTable.appLabel}")
            return ret
        }
    }
    return 0
}

internal fun loadCAPK(posParameter: PosParameter): Int {
    val capkJsonArray = posParameter.capkList ?: return -1
    EMVJNIInterface.emv_capkparam_clear()

    for (i in 0 until capkJsonArray.length()) {
        val item = capkJsonArray.getJSONObject(i)
        val capk = CAPKTable(
            rID = item.rid35,
            modul = item.modulus37.replace("\n", ""),
            capki = item.keyIndex32,
            hashIndex = item.hashAlgorithm36.hexByte,
            arithIndex = item.keyAlgorithm40.hexByte,
            expiry = byteArrayOf(37, 18, 49).hexString,
            exponent = item.exponent38,
//                checkSum = item.hash39
        )
        val dataBuffer = capk.getDataBuffer()
        val ret = EMVJNIInterface.emv_capkparam_add(dataBuffer, dataBuffer.size)
        if (ret < 0) {
            debug("Failed to load capk ${capk.rID + "_" + capk.capki}")
            return ret
        }
    }
    return 0
}

internal fun setEMVTermInfo(posConfig: PosConfig, posParameter: PosParameter) {
    val managementData = posParameter.managementData
    val terminalId = posConfig.terminalId
    val termInfo = ByteArray(256)
    var offset = 0
    // 5F2A: Transaction Currency Code
    termInfo[offset] = 0x5F.toByte()
    termInfo[offset + 1] = 0x2A
    termInfo[offset + 2] = 2
    offset += 3
    System.arraycopy(StringUtil.hexString2bytes(managementData.currencyCode),
        0, termInfo, offset, 2)
    offset += 2
    // 5F36: Transaction Currency Exponent
    termInfo[offset] = 0x5F.toByte()
    termInfo[offset + 1] = 0x36
    termInfo[offset + 2] = 1
    termInfo[offset + 3] = 2 // terminalConfig.currencyExponent
    offset += 4
    // 9F16: Merchant Identification
    if (managementData.cardAcceptorId.length == 15) {
        termInfo[offset] = 0x9F.toByte()
        termInfo[offset + 1] = 0x16
        termInfo[offset + 2] = 15
        offset += 3
        System.arraycopy(managementData.cardAcceptorId.toByteArray(),
            0,
            termInfo,
            offset,
            15)
        offset += 15
    }
    // 9F1A: Terminal Country Code
    termInfo[offset] = 0x9F.toByte()
    termInfo[offset + 1] = 0x1A
    termInfo[offset + 2] = 2
    offset += 3
    System.arraycopy(StringUtil.hexString2bytes(managementData.countryCode),
        0, termInfo, offset, 2)
    offset += 2
    // 9F1C: Terminal Identification
    if (terminalId.length == 8) {
        termInfo[offset] = 0x9F.toByte()
        termInfo[offset + 1] = 0x1C
        termInfo[offset + 2] = 8
        offset += 3
        System.arraycopy(terminalId.toByteArray(),
            0,
            termInfo,
            offset,
            8)
        offset += 8
    }
    // 9F1E: IFD Serial Number
    val ifd = Build.SERIAL
    if (ifd.isNotEmpty()) {
        termInfo[offset] = 0x9F.toByte()
        termInfo[offset + 1] = 0x1E
        termInfo[offset + 2] = ifd.length.toByte()
        offset += 3
        System.arraycopy(ifd.toByteArray(), 0, termInfo, offset, ifd.length)
        offset += ifd.length
    }
    // 9F33: Terminal Capabilities
    termInfo[offset] = 0x9F.toByte()
    termInfo[offset + 1] = 0x33
    termInfo[offset + 2] = 3
    offset += 3
    System.arraycopy(
        byteArrayOf(0xE0.toByte(), 0x40.toByte(), 0xC8.toByte()),
        0, termInfo, offset, 3)
    offset += 3
    // 9F35: Terminal Type
    termInfo[offset] = 0x9F.toByte()
    termInfo[offset + 1] = 0x35
    termInfo[offset + 2] = 1
    termInfo[offset + 3] =
        StringUtil.hexString2bytes(DEFAULT_TERMINAL_TYPE)[0]
    offset += 4
    // 9F40: Additional Terminal Capabilities
    termInfo[offset] = 0x9F.toByte()
    termInfo[offset + 1] = 0x40
    termInfo[offset + 2] = 5
    offset += 3
    System.arraycopy(byteArrayOf(0xE0.toByte(), 0x00, 0xF0.toByte(), 0xA0.toByte(), 0x01),
        0, termInfo, offset, 5)
    offset += 5
    // 9F4E: Merchant Name and Location
    val merNameLength = managementData.cardAcceptorLocation.length
    if (merNameLength > 0) {
        termInfo[offset] = 0x9F.toByte()
        termInfo[offset + 1] = 0x4E
        termInfo[offset + 2] = merNameLength.toByte()
        offset += 3
        System.arraycopy(managementData.cardAcceptorLocation.toByteArray(),
            0,
            termInfo,
            offset,
            merNameLength)
        offset += merNameLength
    }
//        // 9F66: TTQ first byte
//        termInfo[offset] = 0x9F.toByte()
//        termInfo[offset + 1] = 0x66
//        termInfo[offset + 2] = 1
//        termInfo[offset + 3] = terminalConfig.ttq
//        offset += 4
    // DF19: Contactless floor limit
    if (DEFAULT_CONTACTLESS_FLOOR_LIMIT >= 0) {
        termInfo[offset] = 0xDF.toByte()
        termInfo[offset + 1] = 0x19
        termInfo[offset + 2] = 6
        offset += 3
        System.arraycopy(NumberUtil.intToBcd(DEFAULT_CONTACTLESS_FLOOR_LIMIT,
            6),
            0, termInfo, offset, 6)
        offset += 6
    }
    // DF20: Contactless transaction limit
    if (DEFAULT_CONTACTLESS_LIMIT >= 0) {
        termInfo[offset] = 0xDF.toByte()
        termInfo[offset + 1] = 0x20
        termInfo[offset + 2] = 6
        offset += 3
        System.arraycopy(NumberUtil.intToBcd(DEFAULT_CONTACTLESS_LIMIT,
            6),
            0, termInfo, offset, 6)
        offset += 6
    }
    // DF21: CVM limit
    if (DEFAULT_CONTACTLESS_CVM_LIMIT >= 0) {
        termInfo[offset] = 0xDF.toByte()
        termInfo[offset + 1] = 0x21
        termInfo[offset + 2] = 6
        offset += 3
        System.arraycopy(NumberUtil.intToBcd(DEFAULT_CONTACTLESS_CVM_LIMIT, 6),
            0, termInfo, offset, 6)
        offset += 6
    }
    // EF01: Status check support
    termInfo[offset] = 0xEF.toByte()
    termInfo[offset + 1] = 0x01
    termInfo[offset + 2] = 1
    termInfo[offset + 3] = 0 // terminalConfig.statusCheckSupport
    offset += 4
    EMVJNIInterface.emv_terminal_param_set_tlv(termInfo, offset)
}

class WizarTerminalConfig(
    context: Context,
    private val prefs: SharedPreferences = context.getEncryptedSharedPreferences(
        WIZAR_TERMINAL_CONFIG_FILE_NAME),
) : SharedPreferences by prefs {
    var transactionSequenceNumber by intStore("0x9F41", 1)
    var lastTvr by stringStore("0x95")
    var lastTsi by stringStore("0x9B")
//    var uploadType by intStore("0x9B", 0)
}