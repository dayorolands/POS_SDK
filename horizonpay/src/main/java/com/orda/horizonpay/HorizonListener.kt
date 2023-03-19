package com.orda.horizonpay

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import androidx.core.content.edit
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.PinBlockHelper
import com.cluster.core.util.SharedPref
import com.cluster.core.util.debug
import com.cluster.core.util.debugOnly
import com.cluster.pos.EmvException
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.cluster.pos.utils.asDesEdeKey
import com.cluster.pos.utils.encrypt
import com.horizonpay.R
import com.horizonpay.smartpossdk.aidl.IAidlDevice
import com.horizonpay.smartpossdk.aidl.emv.*
import com.horizonpay.smartpossdk.aidl.pinpad.AidlPinPadInputListener
import com.horizonpay.smartpossdk.aidl.pinpad.DukptEncryptObj
import com.horizonpay.smartpossdk.aidl.pinpad.DukptObj
import com.horizonpay.smartpossdk.data.EmvConstant
import com.horizonpay.smartpossdk.data.PinpadConst
import com.orda.horizonpay.utils.EmvUtil
import com.orda.horizonpay.utils.HexUtil
import com.orda.horizonpay.utils.TlvData
import com.orda.horizonpay.utils.TlvDataList
import org.koin.core.component.KoinComponent
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class HorizonListener(
    private val activity: CreditClubActivity,
    device: IAidlDevice,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>
) : AidlEmvStartListener.Stub(), KoinComponent {
    private var hasResumed: Boolean = false
    private val mEmvL2 = device.emvL2
    private val mPinPad = device.getPinpad(true)
    private val cardData = HorizonCardData()
    private val dialogProvider = activity.dialogProvider
    private var field55 : String? = null
    private val prefs = activity.getEncryptedSharedPreferences("com.horizonpay.manager")
    private val pinBlockHelper = PinBlockHelper()
    private val WORKING_KEY_INDEX = 0
    private val MASTER_KEY_INDEX = 0

    override fun onRequestAmount() {
        mEmvL2.requestAmountResp(sessionData.amount.toString())
    }

    override fun onRequestAidSelect(p0: Int, candidateAID: MutableList<CandidateAID>?) {
        Log.d("SunmiListener", "Candidate AID $candidateAID")
        selectApp(candidateAID!!)
    }

    override fun onFinalSelectAid(p0: EmvFinalSelectData?) {
        Log.d("SunmiListener", p0.toString())
    }

    override fun onConfirmCardNo(cardNo: String?) {
        dialogProvider.hideProgressBar()
        cardData.pan = cardNo!!
        dialogProvider.confirm(
            "Confirm Card Pan",
            cardData.pan
        ){
            onSubmit {
                dialogProvider.showProgressBar(R.string.processing)
                mEmvL2.confirmCardNoResp(true)
            }
            onClose {
                continuation.resume(cardData.apply {
                    status = CardTransactionStatus.UserCancel
                })
            }
        }
    }

    override fun onRequestPin(isOnlinePin: Boolean, leftTimes: Int) {
        dialogProvider.hideProgressBar()
        if(!isOnlinePin && leftTimes < 1){
            mEmvL2.stopEmvProcess()
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(HorizonCardData().apply {
                    status = CardTransactionStatus.CardRestricted
                })
            }
            return
        }

        if(isOnlinePin){
            inputOnlinePin(cardData.pan.replace("F", ""))
        } else {
            inputOfflinePin(cardData.pan.replace("F", ""), leftTimes)
        }
    }

    private fun inputOnlinePin(cardPan : String){
        val bundle = setupPinPad(true)
        mPinPad.setKeyAlgorithm(PinpadConst.KeyAlgorithm.DES)
        val masterKey = "3ECEDA9BF4DCCB0B105708E5B334E308"
        val masterKeyKcv = masterKey.padEnd(32, '0').hexBytes.asDesEdeKey.encrypt(ByteArray(8))
        mPinPad.injectSecureTMK(MASTER_KEY_INDEX, MASTER_KEY_INDEX, masterKey.hexBytes, masterKeyKcv)
        val pinkey = "7CD53B62D66BD5574F928AF7F1D03752"
        val pinKeyKcv = HexUtil.hexStringToByte("00000000")
        mPinPad.injectWorkKey(WORKING_KEY_INDEX, PinpadConst.PinPadKeyType.TPINK, pinkey.hexBytes, pinKeyKcv)
        activity.runOnUiThread {
            mPinPad.inputOnlinePin(bundle, intArrayOf(4,6), 30, cardPan, 0, PinpadConst.PinAlgorithmMode.ISO9564FMT1, object : AidlPinPadInputListener.Stub(){
                override fun onConfirm(pinblockData: ByteArray?, noPin: Boolean, ksnData: String?) {
                    mEmvL2.requestPinResp(pinblockData, noPin)

                    //to decrypt the pin block now
                    val decryptPinblock = tripleDesDecrypt(pinkey.hexBytes, pinblockData!!)
                    val secretKey = pinkey.hexBytes.asDesEdeKey.encrypt(decryptPinblock).copyOf(8)
                    cardData.pinBlock = secretKey.hexString
                }

                override fun onSendKey(keyCode: Int) {
                    Log.d("CheckDUKPT", "OnSendKey: $keyCode")
                }

                override fun onCancel() {
                    mEmvL2.requestPinResp(null, false)
                }

                override fun onError(p0: Int) {
                    mEmvL2.requestPinResp(null, false)
                }

            })
        }

    }

    fun tripleDesDecrypt(pinKey: ByteArray, encryptedPinBlock: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        val secretKeySpec = SecretKeySpec(pinKey, "DESede")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(encryptedPinBlock)
    }

    private fun incrementKsn(context: Context, iKsn: String) : String {
        var ksnValue = iKsn
        ksnValue = ksnValue.substring(0, ksnValue.length - 5)
        val counter = SharedPref[context, "ksnCounter", "0"]!!.toLong().plus(1)
        SharedPref[activity, "ksnCounter"] = counter.toString()
        if(counter > 99997){
            SharedPref[activity, "ksnCounter"] = PosManager.KsnCounter
        }
        ksnValue += counter.toString().padStart(5, '0')
        return ksnValue
    }

    private fun inputOfflinePin(pan: String, leftTimes : Int){
        val bundle = Bundle()
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_NEW_LAYOUT, true)
        bundle.putString(PinpadConst.PinpadShow.COMMON_OK_TEXT, "Pin OK")
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_SUPPORT_BYPASS, false)
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_SUPPORT_KEYVOICE, false)
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_IS_RANDOM, true)
        when(leftTimes){
            3 -> bundle.putString(PinpadConst.PinpadShow.TITLE_HEAD_CONTENT, "Please Enter PIN:")
            2 -> bundle.putString( PinpadConst.PinpadShow.TITLE_HEAD_CONTENT, "Please Enter PIN:(2 Chances Left)")
            1 -> bundle.putString( PinpadConst.PinpadShow.TITLE_HEAD_CONTENT, "Please Enter PIN:(Last Chances)")
        }

        val pinPadInputListener = object : AidlPinPadInputListener.Stub(){
            override fun onConfirm(data: ByteArray?, noPin: Boolean, s: String?) {
                cardData.pinBlock = ""
                cardData.ksnData = ""
                mEmvL2.requestPinResp(data, noPin)
            }

            override fun onSendKey(keyCode: Int) {
                Log.d("CheckPinEntry", "OnSendKey: $keyCode")
            }

            override fun onCancel() {
                mEmvL2.requestPinResp(null, false)
            }

            override fun onError(p0: Int) {
                mEmvL2.requestPinResp(null, false)
            }

        }
        mPinPad.inputOfflinePin(bundle, intArrayOf(4), 30, pinPadInputListener)
    }

    private fun setupPinPad(isOnlinePin: Boolean) : Bundle {
        val bundle = Bundle()
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_NEW_LAYOUT, true)
        bundle.putString(PinpadConst.PinpadShow.COMMON_OK_TEXT, "Enter")
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_SUPPORT_BYPASS, false)
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_SUPPORT_KEYVOICE, false)
        bundle.putBoolean(PinpadConst.PinpadShow.COMMON_IS_RANDOM, true)
        if(isOnlinePin){
            bundle.putString(PinpadConst.PinpadShow.TITLE_HEAD_CONTENT, "Please Enter Online PIN")
        } else{
            bundle.putString(PinpadConst.PinpadShow.TITLE_HEAD_CONTENT, "Please Enter Offline PIN")
        }

        return bundle
    }

    override fun onResquestOfflinePinDisp(p0: Int) {
        activity.runOnUiThread {
            dialogProvider.showProgressBar("Processing")
        }
    }

    override fun onRequestOnline(p0: EmvTransOutputData?) {
        activity.runOnUiThread {
            dialogProvider.showProgressBar("Processing")
        }
        val responseCode = "00"
        val iccData = onlineProcessing()
        mEmvL2.requestOnlineResp(responseCode, iccData)
    }

    override fun onFinish(emvResult: Int, emvTransOutputData: EmvTransOutputData?) {
        if(emvResult != EmvConstant.EmvTransResultCode.SUCCESS){
            val exception = EmvException("Horizon Emv failed with ret $emvResult")
            debugOnly { Log.e("HorizonK11", exception.message, exception) }
        }

        when(emvResult) {
            EmvConstant.EmvTransResultCode.SUCCESS -> {
                cardData.mIccString = getField55String()
                cardData.apply {
                    track2 = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_TRACK2DATA)
                    var markerIndex = track2.indexOf("D")
                    if (markerIndex < 0) {
                        markerIndex = track2.indexOf("=")
                    }
                    src = track2.substring(markerIndex + 5, markerIndex + 8)
                    status = CardTransactionStatus.Success
                    transactionAmount = mEmvL2.getTagValue(EmvTags.EMV_TAG_TM_AUTHAMNTN)
                    exp = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_APPEXPIREDATE)
                    holder = String(mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_CHNAME).hexBytes)
                    cardSequenceNumber = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_PANSN)
                    aid = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_AID)
                    cardMethod = CardReaderEvent.CHIP
                }
                val tvr = mEmvL2.getTagValue(EmvTags.EMV_TAG_TM_TVR).hexBytes
                if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                    cardData.ret = CardTransactionStatus.OfflinePinVerifyError.code
                    return
                }
            }
            else -> {
                cardData.status = CardTransactionStatus.Failure
            }
        }
        if(!hasResumed){
            hasResumed = true
            continuation.resume(cardData)
        }
    }

    internal inline val ByteArray.hexString: String
        get() {
            val stringBuilder = StringBuilder("")
            if (size <= 0) return ""
            val buffer = CharArray(2)
            for (i in indices) {
                buffer[0] = Character.forDigit(get(i).toInt() ushr 4 and 0x0F, 16)
                buffer[1] = Character.forDigit(get(i).toInt() and 0x0F, 16)
                stringBuilder.append(buffer)
            }
            return stringBuilder.toString().uppercase(Locale.getDefault())
        }

    override fun onError(p0: Int) {
        debugOnly { Log.d("HorizonK11", "Error : $p0") }
        mEmvL2.stopEmvProcess()
        if (!hasResumed) {
            hasResumed = true
            continuation.resume(HorizonCardData().apply {
                status = CardTransactionStatus.Error
            })
        }
    }

    private fun selectApp(appList: MutableList<CandidateAID>) {
        val options = arrayOfNulls<String>(appList.size)
        for (i in appList.indices) {
            options[i] = appList[i].appLabel
        }
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle("Please select app")
        alertBuilder.setItems(
            options
        ) { _, index ->
            try {
                mEmvL2.requestAidSelectResp(index)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        val newAlertDialog = alertBuilder.create()
        newAlertDialog.show()
    }

    private fun onlineProcessing() : String{
        val builder = StringBuilder()
        var arqcTlv = mEmvL2.getTlvByTags(EmvUtil.arqcTLVTags)
        builder.append(arqcTlv)
        Log.d("CheckPinEntry", "Here is the arqcTlv $arqcTlv")
        if(!TlvDataList.fromBinary(arqcTlv).contains(EmvTags.EMV_TAG_IC_CID)){
            builder.append(TlvData.fromData(EmvTags.EMV_TAG_IC_CID, byteArrayOf(0x80.toByte())))
        }

        val tlvDataList = TlvDataList.fromBinary(builder.toString())
        arqcTlv = tlvDataList.toString()

        val apn = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_APNAME)
        val appLabel = mEmvL2.getTagValue(EmvTags.EMV_TAG_IC_APPLABEL)
        val resultTlv = (arqcTlv + TlvData.fromData(
            "AC",
            TlvDataList.fromBinary(arqcTlv).getTLV(EmvTags.EMV_TAG_IC_AC).bytesValue
        )
                + TlvData.fromData(EmvTags.EMV_TAG_TM_TSI, ByteArray(2))
                + TlvData.fromData(EmvTags.EMV_TAG_TM_CVMRESULT, ByteArray(3))
            .toString() +
                (
                if (TextUtils.isEmpty(appLabel)) ""
                else
                    TlvData.fromData(EmvTags.EMV_TAG_IC_APPLABEL, HexUtil.hexStringToByte(appLabel))
                )
            .toString() + if (TextUtils.isEmpty(apn)) "" else TlvData.fromData(
            EmvTags.EMV_TAG_IC_APNAME,
            HexUtil.hexStringToByte(apn)
        ))

        Log.d("CheckPinEntry", "The online result $resultTlv")

        val arpcTlv = EmvUtil.getExampleARPCData() ?: return ""

        return HexUtil.bytesToHexString(arpcTlv)
    }

    private fun getField55String(): String {
        if (field55 != null) return field55!!
        val tags = arrayOf(
            "82",
            "84",
            "95",
            "9F26",
            "9F27",
            "9F10",
            "9F37",
            "9F36",
            "9A",
            "9C",
            "9F02",
            "9F03",
            "5F2A",
            "9F1A",
            "9F03",
            "9F33",
            "9F34",
            "9F35",
            "9F09",
            "9F41"
        )
        field55 = mEmvL2.getTlvByTags(tags)
        return field55!!
    }
}