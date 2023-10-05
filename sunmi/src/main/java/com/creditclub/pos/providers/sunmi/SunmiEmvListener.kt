package com.creditclub.pos.providers.sunmi

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import bsh.Interpreter
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.PinBlockHelper
import com.cluster.core.util.debug
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.cluster.pos.providers.sunmi.R
import com.cluster.pos.utils.asDesEdeKey
import com.cluster.pos.utils.encrypt
import com.creditclub.pos.providers.sunmi.emv.TLV
import com.creditclub.pos.providers.sunmi.emv.TLVUtil
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidl.bean.CardInfo
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2
import org.koin.core.component.KoinComponent
import sunmi.paylib.SunmiPayKernel
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume


/**
 * Created by Ifedayo Adekoya <ifedayo.adekoya@starkitchensgroup.com> on 06/02/2023.
 * Orda
 */

class SunmiEmvListener(
    private val activity: CreditClubActivity,
    kernel: SunmiPayKernel,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>,
) : EMVListenerV2.Stub(), KoinComponent {
    private var mProcessStep: Int = 0
    private var mReadCardOptV2 = kernel.mReadCardOptV2
    private var mPinPadOptV2 = kernel.mPinPadOptV2
    private var mEMVOptV2 = kernel.mEMVOptV2
    private val dialogProvider = activity.dialogProvider
    private val pinBlockHelper = PinBlockHelper()

    private val cardData = SunmiCardData()

    private val mCardType = 0
    private var mPinType = 0 // 0-online pin, 1-offline pin
    private var mCertInfo: String? = null
    private var mSelectIndex = 0

    private var mAppSelect = 0
    private val mSecurityOptV2 = kernel.mSecurityOptV2

    private val prefs = activity.getEncryptedSharedPreferences("com.sunmi.manager")

    @Throws(RemoteException::class)
    override fun onWaitAppSelect(appNameList: List<EMVCandidateV2>, isFirstSelect: Boolean) {
        Log.d("DetectCard", "onWaitAppSelect >>>> $appNameList , $isFirstSelect")
        debug("onWaitAppSelect isFirstSelect:$isFirstSelect")
        mProcessStep = EMV_APP_SELECT
        val candidateNames: Array<String> = getCandidateNames(appNameList)
        mHandler.obtainMessage(EMV_APP_SELECT, candidateNames)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onAppFinalSelect(tag9F06value: String?) {
        debug("onAppFinalSelect tag9F06value >>>> $tag9F06value")
        Log.d("DetectCard", "onAppFinalSelect $tag9F06value")

        run {
            // set normal tlv data
            val tags = arrayOf("5F2A", "5F36")
            val values = arrayOf("0566", "00")
            mEMVOptV2.setTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, values)
        }

        if (!tag9F06value.isNullOrEmpty()) {
            val isVisa = tag9F06value.startsWith("A000000003")
            val isMaster = tag9F06value.startsWith("A000000004")
            val isUnion = tag9F06value.startsWith("A000000333")
            when {
                isVisa -> {
                    // VISA(PayWave)
                    debug("detect VISA card")
                    mAppSelect = 1
                    // set PayWave tlv data
                    val tagsPayWave = arrayOf(
                        "DF8124", "DF8125", "DF8126"
                    )
                    val valuesPayWave = arrayOf(
                        "999999999999", "999999999999", "000000000000"
                    )
                    mEMVOptV2.setTlvList(
                        AidlConstants.EMV.TLVOpCode.OP_PAYWAVE,
                        tagsPayWave,
                        valuesPayWave
                    )
                }
                isMaster -> {
                    // MasterCard(PayPass)
                    debug("detect MasterCard card")
                    mAppSelect = 2
                    // set PayPass tlv data
                    val tagsPayPass = arrayOf(
                        "DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                        "DF8123", "DF8124", "DF8125", "DF8126",
                        "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"
                    )
                    val valuesPayPass = arrayOf(
                        "E0", "F8", "F8", "E8", "00", "00",
                        "999999999999", "999999999999", "999999999999", "000000000000",
                        "30", "02", "0000000000", "000000000000", "000000000000"
                    )
                    mEMVOptV2.setTlvList(
                        AidlConstants.EMV.TLVOpCode.OP_PAYPASS,
                        tagsPayPass,
                        valuesPayPass
                    )
                }
                isUnion -> {
                    mAppSelect = 0
                    // UnionPay
                    debug("detect UnionPay card")
                }
            }
            if (AidlConstants.CardType.IC.value == mCardType) {
                val tags = arrayOf("9F33", "9F09", "DF81FF")
                val values = arrayOf("E008FF", "0111", "01")
                mEMVOptV2.setTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, values)
            }
        }
        mProcessStep = EMV_FINAL_APP_SELECT
        mHandler.obtainMessage(EMV_FINAL_APP_SELECT, tag9F06value).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onConfirmCardNo(cardNo: String) {
        debug("onConfirmCardNo cardNo:$cardNo")
        Log.d("DetectCard", "onConfirmCardNo >>>> Card pan: $cardNo")
        cardData.pan = cardNo
        mProcessStep = EMV_CONFIRM_CARD_NO
        mHandler.obtainMessage(EMV_CONFIRM_CARD_NO)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onRequestShowPinPad(pinType: Int, remainTime: Int) {
        Log.d("DetectCard", "onRequestShowPinPad >>>>> pinType: $pinType remainTime: $remainTime")
        Log.d("CheckDUKPT","onRequestShowPinPad pinType:$pinType remainTime:$remainTime")
        mPinType = pinType
        if(cardData.pan.isEmpty()){
            cardData.pan = getCardNo()
            Log.d("DetectCard", "The new card pan >>>>>> ${cardData.pan}")
        }
        mProcessStep = EMV_SHOW_PIN_PAD
        mHandler.obtainMessage(EMV_SHOW_PIN_PAD)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onRequestSignature() {
        Log.d("DetectCard", "onRequestSignature >>>>>> request signature")
        debug("onRequestSignature")
        mProcessStep = EMV_SIGNATURE
        mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onCertVerify(certType: Int, certInfo: String) {
        Log.d("DetectCard", "onCertVerify >>>>>> on request ceritificate verification")
        debug("onCertVerify certType:$certType certInfo:$certInfo")
        mCertInfo = certInfo
        mProcessStep = EMV_CERT_VERIFY
        mHandler.obtainMessage(EMV_CERT_VERIFY).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onOnlineProc() {
        Log.d("DetectCard", "onOnlineProcess >>>>>>")
        debug("onOnlineProcess")
        mProcessStep = EMV_ONLINE_PROCESS
        mHandler.obtainMessage(EMV_ONLINE_PROCESS)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onCardDataExchangeComplete() {
        Log.d("DetectCard", "onCardDataExchangeComplete >>>>>>>")
        debug("onCardDataExchangeComplete")
    }

    @Throws(RemoteException::class)
    override fun onTransResult(code: Int, desc: String?) {
        debug("onTransResult code:$code desc:$desc")
        Log.d("DetectCard", "onTransResult >>>>>>> code : $code desc: $desc")
        debug("***************************************************************")
        debug("****************************End Process************************")
        debug("***************************************************************")
        if (code != 0) {
            mHandler.obtainMessage(
                EMV_TRANS_FAIL,
                code,
                code,
                desc
            ).sendToTarget()
        } else {
            mHandler.obtainMessage(
                EMV_TRANS_SUCCESS,
                code,
                code,
                desc
            ).sendToTarget()
        }
    }

    @Throws(RemoteException::class)
    override fun onConfirmationCodeVerified() {
        debug("onConfirmationCodeVerified")
        val outData = ByteArray(512)
        val len: Int = mEMVOptV2.getTlv(AidlConstants.EMV.TLVOpCode.OP_PAYPASS, "DF8129", outData)
        if (len > 0) {
            val data = ByteArray(len)
            System.arraycopy(outData, 0, data, 0, len)
            debug("DF8129: ${data.hexString}")
        }

        // card off
        mReadCardOptV2.cardOff(mCardType)
    }

    override fun onRequestDataExchange(p0: String?) {
        Log.d("DetectCard", "onRequestDataExchange >>>>>>>")
    }

    override fun onTermRiskManagement() {
        Log.d("DetectCard", "onTerminalRiskManagement >>>>>>>")
    }

    override fun onPreFirstGenAC() {
        Log.d("DetectCard", "onPreFirstGenAC >>>>>>>")
    }

    override fun onDataStorageProc(p0: Array<out String>?, p1: Array<out String>?) {
        Log.d("DetectCard", "onDataStorageProcessing >>>>>>")
    }

    private val mLooper = Looper.myLooper()
    private val mHandler: Handler = object : Handler(mLooper!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                EMV_APP_SELECT -> {
                    dialogProvider.hideProgressBar()
                    val candiNames = msg.obj as Array<*>
                    val options = candiNames.map { DialogOptionItem(it as String) }
                    dialogProvider.showOptions("Select App", options) {
                        onSubmit {
                            dialogProvider.showProgressBar(R.string.handling)
                            mSelectIndex = it
                            mEMVOptV2.importAppSelect(0)
                        }

                        onClose {
                            mEMVOptV2.importAppSelect(-1)
                        }
                    }
                }
                EMV_FINAL_APP_SELECT -> mEMVOptV2.importAppFinalSelectStatus(0)
                EMV_CONFIRM_CARD_NO -> {
                    dialogProvider.showProgressBar(R.string.handling)
                    mEMVOptV2.importCardNoStatus(0)
                }
                EMV_CERT_VERIFY -> {
                    dialogProvider.showProgressBar(R.string.handling)
                    mEMVOptV2.importCertStatus(0)
                }
                EMV_SHOW_PIN_PAD -> {
                    dialogProvider.hideProgressBar()
                    initPinPad()
                }
                EMV_ONLINE_PROCESS -> mockRequestToServer()
                EMV_SIGNATURE -> mEMVOptV2.importSignatureStatus(0)
                PIN_CLICK_NUMBER -> {
                }
                PIN_CLICK_PIN -> importPinInputStatus(0)
                PIN_CLICK_CONFIRM -> importPinInputStatus(2)
                PIN_CLICK_CANCEL -> {
                    debug("user cancel")
                    importPinInputStatus(1)
                }
                PIN_ERROR -> {
                    debug("error:" + msg.obj + " -- " + msg.arg1)
                    importPinInputStatus(3)
                }
                EMV_TRANS_FAIL -> {
                    cardData.status = CardTransactionStatus.Failure
                    dialogProvider.hideProgressBar()
                    continuation.resume(cardData)
                }
                EMV_TRANS_SUCCESS -> {
                    cardData.status = CardTransactionStatus.Success
                    dialogProvider.hideProgressBar()
                    continuation.resume(cardData)
                }
            }
        }
    }

    private fun initPinPad() {
        val posParameter = sessionData.getPosParameter?.invoke()

        val pinPadConfig = PinPadConfigV2().apply {
            pinPadType = 0
            pinType = mPinType
            isOrderNumKey = false
            val panBytes = cardData.pan
                .substring(cardData.pan.length - 13, cardData.pan.length - 1)
                .toByteArray(charset("US-ASCII"))
            pan = panBytes
            timeout = 60 * 1000 // input password timeout
            pinKeyIndex = 12 // pik index
            maxInput = 4
            minInput = 4
            keySystem = 0
            algorithmType = 0
        }

        val mPinPadListener: PinPadListenerV2 = object : PinPadListenerV2.Stub() {
            override fun onPinLength(len: Int) {
                Interpreter.debug("onPinLength:$len")
                mHandler.obtainMessage(PIN_CLICK_NUMBER, len)
                    .sendToTarget()
            }

            override fun onConfirm(i: Int, pinBlock: ByteArray?) {
                if (pinBlock != null) {
                    mHandler.obtainMessage(PIN_CLICK_PIN, pinBlock).sendToTarget()
                    if(pinBlock.hexString == "00"){
                        cardData.pinBlock = ""
                        cardData.ksnData = ""
                    }
                    else{
                        //Triple DES Encryption
                        val pinkey = "11111111111111111111111111111111".hexBytes
                        val decryptPinblock = tripleDesDecrypt(pinkey, pinBlock)
                        val secretKey = posParameter?.pinKey?.hexBytes?.asDesEdeKey?.encrypt(decryptPinblock)?.copyOf(8)
                        cardData.pinBlock = secretKey!!.hexString
                    }
                } else {
                    mHandler.obtainMessage(PIN_CLICK_CONFIRM)
                        .sendToTarget()
                }
            }

            override fun onCancel() {
                Interpreter.debug("onCancel")
                mHandler.obtainMessage(PIN_CLICK_CANCEL)
                    .sendToTarget()
            }

            override fun onError(code: Int) {
                Interpreter.debug("onError:$code")
                val msg = AidlErrorCodeV2.valueOf(code).msg
                mHandler.obtainMessage(
                    PIN_ERROR,
                    code,
                    code,
                    msg
                ).sendToTarget()
            }
        }
        mPinPadOptV2.initPinPad(pinPadConfig, mPinPadListener)
    }

    fun tripleDesDecrypt(pinKey: ByteArray, encryptedPinBlock: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        val secretKeySpec = SecretKeySpec(pinKey, "DESede")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(encryptedPinBlock)
    }

    private fun importPinInputStatus(inputResult: Int) {
        debug("importPinInputStatus:$inputResult")
        try {
            mEMVOptV2.importPinInputStatus(mPinType, inputResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun importOnlineProcessStatus(status: Int) {
        debug("importOnlineProcessStatus status:$status")
        val tags = arrayOf("71", "72", "91", "8A", "89")
        val values = arrayOf(
            "", "", "", "", ""
        )
        val out = ByteArray(1024)
        val len = mEMVOptV2.importOnlineProcStatus(status, tags, values, out)
        if (len < 0) {
            debug("importOnlineProcessStatus error,code:$len")
        } else {
            val bytes = out.copyOf(len)
            debug("importOnlineProcessStatus outData:${bytes.hexString}")
        }
    }

    private fun mockRequestToServer() {
        Thread {
            try {
                activity.runOnUiThread {
                    dialogProvider.showProgressBar(R.string.handling)
                }
                if (AidlConstants.CardType.MAGNETIC.value != mCardType) {
                    getTlvData()
                }
                Thread.sleep(2000)
                // notice  ==  import the online result to SDK and end the process.
                importOnlineProcessStatus(0)
            } catch (e: Exception) {
                e.printStackTrace()
                importOnlineProcessStatus(-1)
            } finally {
                activity.runOnUiThread {
                    dialogProvider.hideProgressBar()
                }
            }
        }.start()
    }

    private fun getTlvData() {
        val tagList = arrayOf(
            "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
            "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40", "9F03",
            "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E", "84", "4F",
            "9F09", "9F41", "5F20", "9F12", "50", "9F02",
        )

        val tagListForField55 = arrayOf("82", "84", "95", "9F26", "9F27", "9F10", "9F37", "9F36", "9A",
            "9C", "9F02", "9F03", "5F2A", "9F1A", "9F03", "9F33", "9F34", "9F35", "9F09", "9F41"
        )
        val outData = ByteArray(2048)
        val outDataForField55 = ByteArray(1024)
        val tlvOpCode: Int = if (AidlConstants.CardType.NFC.value == mCardType) {
            when (mAppSelect) {
                2 -> AidlConstants.EMV.TLVOpCode.OP_PAYPASS
                1 -> AidlConstants.EMV.TLVOpCode.OP_PAYWAVE
                else -> AidlConstants.EMV.TLVOpCode.OP_NORMAL
            }
        } else {
            AidlConstants.EMV.TLVOpCode.OP_NORMAL
        }
        val len = mEMVOptV2.getTlvList(tlvOpCode, tagList, outData)
        val len55 = mEMVOptV2.getTlvList(tlvOpCode, tagListForField55, outDataForField55)
        if (len > 0) {
            val bytes = outData.copyOf(len)
            cardData.mIccString = outDataForField55.copyOf(len55).hexString
            val tlvMap: Map<String, TLV> = TLVUtil.buildTLVMap(bytes.hexString)

            cardData.apply {
                ret = CardTransactionStatus.Success.code
                transactionAmount = tlvMap.getValue(0x9F02)
                exp = tlvMap.getValue(0x5F24)
                holder = tlvMap.getValue(0x5F20, true)
                cardSequenceNumber = tlvMap.getValue(0x5f34)
                aid = tlvMap.getValue(0x9F06)
                track2 = tlvMap.getValue(0x57, hex = false, fPadded = true)
                var markerIndex = track2.indexOf("D")
                if (markerIndex < 0) {
                    markerIndex = track2.indexOf("=")
                }
                src = track2.substring(markerIndex + 5, markerIndex + 8)
                
                atc = tlvMap.getValue(0x9F36)
                cryptogram = tlvMap.getValue(0x9F26)
                cryptogramInformationData = tlvMap.getValue(0x9F27)
                terminalCapabilities = tlvMap.getValue(0x9F33)
                terminalType = tlvMap.getValue(0x9F35)
                iad = tlvMap.getValue(0x9F10)
                tvr = tlvMap.getValue(0x95)
                unpredictedNumber = tlvMap.getValue(0x9F37)
                dedicatedFileName = tlvMap.getValue(0x84)
                transactionDate = tlvMap.getValue(0x9A)
                transactionType = tlvMap.getValue(0x9C)
                transactionCurrency = "566"
                cardHolderVerificationMethod = tlvMap.getValue(0x9F34)
                amountAuthorized = tlvMap.getValue(0x9F02)
                amountOther = tlvMap.getValue(0x9F03)
                

                cardMethod = CardReaderEvent.CHIP
            }

            val tvr = tlvMap.getValue(0x95).hexBytes
            if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                cardData.status = CardTransactionStatus.OfflinePinVerifyError
            }
        }
    }

    private fun getCardNo(): String {
        try {
            val tagList = arrayOf("57", "5A")
            val outData = ByteArray(256)
            val len: Int = mEMVOptV2.getTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tagList, outData)
            if (len <= 0) {
                Log.d("DetectCard", "getCardNo error,code:$len")
                return ""
            }
            val bytes = outData.copyOf(len)
            val tlvMap = TLVUtil.buildTLVMap(bytes)
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap["57"])?.value)) {
                val tlv57 = tlvMap["57"]
                val cardInfo: CardInfo = parseTrack2(tlv57!!.value)
                return cardInfo.cardNo
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap["5A"])?.value)) {
                return Objects.requireNonNull(tlvMap["5A"])!!.value
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun parseTrack2(track2: String): CardInfo {
        Log.d("DetectCard", "track2:$track2")
        val track_2: String = stringFilter(track2)
        var index = track_2.indexOf("=")
        if (index == -1) {
            index = track_2.indexOf("D")
        }
        val cardInfo = CardInfo()
        if (index == -1) {
            return cardInfo
        }
        var cardNumber = ""
        if (track_2.length > index) {
            cardNumber = track_2.substring(0, index)
        }
        var expiryDate = ""
        if (track_2.length > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5)
        }
        var serviceCode = ""
        if (track_2.length > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8)
        }
        Log.d("DetectCard", ">>>>>>>> cardNumber:$cardNumber expireDate:$expiryDate serviceCode:$serviceCode")
        cardInfo.cardNo = cardNumber
        cardInfo.expireDate = expiryDate
        cardInfo.serviceCode = serviceCode
        return cardInfo
    }

    private fun stringFilter(str: String?): String {
        val regEx = "[^0-9=D]"
        val p: Pattern = Pattern.compile(regEx)
        val matcher: Matcher? = str?.let { p.matcher(it) }
        if (matcher != null) {
            return matcher.replaceAll("").trim { it <= ' ' }
        }

        return ""
    }
}

private fun getCandidateNames(candiList: List<EMVCandidateV2>?): Array<String> {
    if (candiList.isNullOrEmpty()) return emptyArray()
    return Array(candiList.size) { i ->
        val candi = candiList[i]
        var name = candi.appPreName
        name = if (TextUtils.isEmpty(name)) candi.appLabel else name
        name = if (TextUtils.isEmpty(name)) candi.appName else name
        name = if (TextUtils.isEmpty(name)) "" else name
        debug("EMVCandidateV2: $name")
        return@Array name
    }
}

private fun Map<String, TLV>.getValue(
    tag: Int,
    hex: Boolean = false,
    fPadded: Boolean = false,
): String {
    val tagString = tag.toString(16).uppercase(Locale.ROOT)
    val rawValue = get(tagString)!!.value//.substring(tagString.length + 2)

    val value = when {
        hex -> String(rawValue.hexBytes)
        else -> rawValue
    }

    if (fPadded) {
        val stringBuffer = StringBuffer(value)
        if (stringBuffer[stringBuffer.toString().length - 1] == 'F') {
            stringBuffer.deleteCharAt(stringBuffer.toString().length - 1)
        }
        return stringBuffer.toString()
    }

    return value
}

@Throws(
    NoSuchAlgorithmException::class,
    NoSuchProviderException::class,
    NoSuchPaddingException::class,
    InvalidKeyException::class,
    ShortBufferException::class,
    IllegalBlockSizeException::class,
    BadPaddingException::class,
    InvalidAlgorithmParameterException::class
)
fun tdesECBEncypt(keyBytes: ByteArray, input: ByteArray): ByteArray {
    val key = SecretKeySpec(keyBytes, "DES")
    val cipher: Cipher = Cipher.getInstance(
        "DESede/ECB/NoPadding"
    )
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val cipherText = ByteArray(cipher.getOutputSize(input.size))
    var ctLength = cipher.update(input, 0, input.size, cipherText, 0)
    ctLength += cipher.doFinal(cipherText, ctLength)
    return cipherText
}

private const val EMV_APP_SELECT = 1
private const val EMV_FINAL_APP_SELECT = 2
private const val EMV_CONFIRM_CARD_NO = 3
private const val EMV_CERT_VERIFY = 4
private const val EMV_SHOW_PIN_PAD = 5
private const val EMV_ONLINE_PROCESS = 6
private const val EMV_SIGNATURE = 7
private const val EMV_TRANS_SUCCESS = 888
private const val EMV_TRANS_FAIL = 999

private const val PIN_CLICK_NUMBER = 50
private const val PIN_CLICK_PIN = 51
private const val PIN_CLICK_CONFIRM = 52
private const val PIN_CLICK_CANCEL = 53
private const val PIN_ERROR = 54
