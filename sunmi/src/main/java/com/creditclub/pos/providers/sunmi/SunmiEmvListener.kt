package com.creditclub.pos.providers.sunmi

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.RemoteException
import android.text.TextUtils
import androidx.core.content.edit
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.util.debug
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardTransactionStatus
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.extensions.hexString
import com.creditclub.pos.providers.sunmi.emv.TLV
import com.creditclub.pos.providers.sunmi.emv.TLVUtil
import com.creditclub.pos.utils.asDesEdeKey
import com.creditclub.pos.utils.encrypt
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2
import org.koin.core.KoinComponent
import org.koin.core.get
import sunmi.paylib.SunmiPayKernel
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

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
        debug("onWaitAppSelect isFirstSelect:$isFirstSelect")
        mProcessStep = EMV_APP_SELECT
        val candidateNames: Array<String> = getCandidateNames(appNameList)
        mHandler.obtainMessage(EMV_APP_SELECT, candidateNames)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onAppFinalSelect(tag9F06value: String?) {
        debug("onAppFinalSelect tag9F06value:$tag9F06value")

        run {
            // set normal tlv data
            val tags = arrayOf("5F2A", "5F36")
            val values = arrayOf("0566", "00")
            mEMVOptV2.setTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, tags, values)
        }

        if (tag9F06value != null && tag9F06value.isNotEmpty()) {
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
        cardData.pan = cardNo
        mProcessStep = EMV_CONFIRM_CARD_NO
        mHandler.obtainMessage(EMV_CONFIRM_CARD_NO)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onRequestShowPinPad(pinType: Int, remainTime: Int) {
        debug("onRequestShowPinPad pinType:$pinType remainTime:$remainTime")
        mPinType = pinType
        mProcessStep = EMV_SHOW_PIN_PAD
        mHandler.obtainMessage(EMV_SHOW_PIN_PAD)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onRequestSignature() {
        debug("onRequestSignature")
        mProcessStep = EMV_SIGNATURE
        mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onCertVerify(certType: Int, certInfo: String) {
        debug("onCertVerify certType:$certType certInfo:$certInfo")
        mCertInfo = certInfo
        mProcessStep = EMV_CERT_VERIFY
        mHandler.obtainMessage(EMV_CERT_VERIFY).sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onOnlineProc() {
        debug("onOnlineProcess")
        mProcessStep = EMV_ONLINE_PROCESS
        mHandler.obtainMessage(EMV_ONLINE_PROCESS)
            .sendToTarget()
    }

    @Throws(RemoteException::class)
    override fun onCardDataExchangeComplete() {
        debug("onCardDataExchangeComplete")
    }

    @Throws(RemoteException::class)
    override fun onTransResult(code: Int, desc: String?) {
        debug("onTransResult code:$code desc:$desc")
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
//        activity.runOnUiThread {
//            dialogProvider.confirm("See Phone", "execute See Phone flow") {
//                onSubmit {
//                    mEMVOptV2.initEmvProcess()
//                    checkCard()
//                }
//            }
//        }
    }

    private val mLooper = Looper.myLooper()
    private val mHandler: Handler = object : Handler(mLooper!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                EMV_FINAL_APP_SELECT -> mEMVOptV2.importAppFinalSelectStatus(0)
                EMV_APP_SELECT -> {
                    dialogProvider.hideProgressBar()
                    val candiNames = msg.obj as Array<String>
                    val options = candiNames.map { DialogOptionItem(it) }
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
                EMV_CONFIRM_CARD_NO -> {
                    dialogProvider.hideProgressBar()
                    dialogProvider.confirm(
                        activity.getString(R.string.emv_confirm_card_no),
                        cardData.pan,
                    ) {
                        onSubmit {
                            dialogProvider.showProgressBar(R.string.handling)
                            mEMVOptV2.importCardNoStatus(0)
                        }

                        onClose {
                            continuation.resume(cardData.apply {
                                status = CardTransactionStatus.UserCancel
                            })
                        }
                    }
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
                    continuation.resume(cardData)
                    dialogProvider.hideProgressBar()
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
        val amount = sessionData.amount / 100.0
        var amountText = ""
        if (sessionData.amount > 0) amountText = "Amount: ${amount.toCurrencyFormat()}"
        if (sessionData.cashBackAmount > 0) amountText = "$amount        " +
                "Cashback Amount: ${amount.toCurrencyFormat()}"
        val dukptConfig = sessionData.getDukptConfig?.invoke(cardData.pan, amount)
        val posParameter: PosParameter =
            sessionData.getPosParameter?.invoke(cardData.pan, sessionData.amount / 100.0) ?: get()

        if (dukptConfig != null) {
            val ipekBytes = dukptConfig.ipek.hexBytes
            val ipekBytesPadded = dukptConfig.ipek.padEnd(32, '0').hexBytes
            val ksnBytes = dukptConfig.ksn.hexBytes
//            val cipheyKey = ipekBytes.asDesEdeKey
//            val kcv = cipheyKey.encrypt(ByteArray(8)).hexString
            val kcv = tdesECBEncypt(ipekBytesPadded, ByteArray(8)).hexString
            if (prefs.getString("kcv", null) != kcv) {
                val result = mSecurityOptV2.saveKeyDukpt(
                    AidlConstants.Security.KEY_TYPE_DUPKT_IPEK,
                    ipekBytes,
                    kcv.hexBytes,
                    ksnBytes,
                    AidlConstants.Security.KEY_ALG_TYPE_3DES,
                    0,
                )
                debug("Dukpt inject result is $result")
                if (result == 0) prefs.edit { putString("kcv", kcv) }
            }
            mSecurityOptV2.dukptIncreaseKSN(0)
        } else {
            val masterKey = posParameter.masterKey.hexBytes
            mSecurityOptV2.savePlaintextKey(
                AidlConstants.Security.KEY_TYPE_TMK,
                masterKey,
                masterKey.asDesEdeKey.encrypt(ByteArray(8)),
                AidlConstants.Security.KEY_ALG_TYPE_3DES,
                0,
            )
            val tempPinKey = posParameter.pinKey.hexBytes
            val pinKey = ByteArray(24)
            System.arraycopy(tempPinKey, 0, pinKey, 0, 16)
            System.arraycopy(tempPinKey, 0, pinKey, 16, 8)

            mSecurityOptV2.savePlaintextKey(
                AidlConstants.Security.KEY_TYPE_PIK,
                pinKey,
                pinKey.asDesEdeKey.encrypt(ByteArray(8)),
                AidlConstants.Security.KEY_ALG_TYPE_3DES,
                0,
            )
        }

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
            keySystem = if (dukptConfig == null) 0 else 1
            algorithmType = 0
        }

        val mPinPadListener: PinPadListenerV2 = object : PinPadListenerV2.Stub() {
            override fun onPinLength(len: Int) {
                debug("onPinLength:$len")
                mHandler.obtainMessage(PIN_CLICK_NUMBER, len)
                    .sendToTarget()
            }

            override fun onConfirm(i: Int, pinBlock: ByteArray?) {
                if (pinBlock != null) {
                    debug("onConfirm pin block:${pinBlock.hexString}")
                    mHandler.obtainMessage(PIN_CLICK_PIN, pinBlock).sendToTarget()
                    cardData.pinBlock = pinBlock.hexString
                    if (dukptConfig != null) {
                        val ksnOutData = ByteArray(0)
                        mSecurityOptV2.dukptCurrentKSN(0, ksnOutData)
                        cardData.ksnData = ksnOutData.hexString
                    }
                } else {
                    mHandler.obtainMessage(PIN_CLICK_CONFIRM)
                        .sendToTarget()
                }
            }

            override fun onCancel() {
                debug("onCancel")
                mHandler.obtainMessage(PIN_CLICK_CANCEL)
                    .sendToTarget()
            }

            override fun onError(code: Int) {
                debug("onError:$code")
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
                Thread.sleep(1500)
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
        val outData = ByteArray(2048)
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
        if (len > 0) {
            val bytes = outData.copyOf(len)
            cardData.mIccString = bytes.hexString
            val tlvMap: Map<String, TLV> = TLVUtil.buildTLVMap(cardData.mIccString)

            cardData.apply {
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

                cardMethod = CardReaderEvent.CHIP
            }
            val tvr = tlvMap.getValue(0x95).hexBytes
            if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                cardData.status = CardTransactionStatus.OfflinePinVerifyError
            }
        }
    }
}

private fun getCandidateNames(candiList: List<EMVCandidateV2>?): Array<String> {
    if (candiList == null || candiList.isEmpty()) return emptyArray()
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
    val tagString = tag.toString(16).toUpperCase(Locale.ROOT)
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
        "DESede/ECB/NoPadding",
//        BouncyCastleProvider(),
    )
    cipher.init(1, key)
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
