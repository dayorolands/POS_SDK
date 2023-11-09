package com.urovo.v67

import android.device.SEManager
import android.graphics.Color
import android.os.Bundle
import android.os.IInputActionListener
import android.util.Log
import androidx.core.content.edit
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.mask
import com.cluster.core.util.toCurrencyFormat
import com.cluster.pos.PosManager
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.cluster.pos.utils.asDesEdeKey
import com.cluster.pos.utils.encrypt
import com.nexgo.R
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import com.urovo.sdk.pinpad.utils.Constant
import com.urovo.sdk.utils.BytesUtil
import org.koin.core.component.KoinComponent
import java.util.Hashtable
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class UrovoListener(
    private val activity: CreditClubActivity,
    private val emvNfcKernelApi: EmvNfcKernelApi,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>,
    private val defaultPosParameter: PosParameter
) : EmvListener, KoinComponent {
    private var hasResumed: Boolean = false
    private var isOnlinePinEntered : Boolean = false
    private val dialogProvider = activity.dialogProvider
    private val cardData = UrovoCardData()
    private var field55: String? = null
    private val pinPadProviderImpl: PinPadProviderImpl = PinPadProviderImpl.getInstance()
    private val prefs = activity.getEncryptedSharedPreferences("com.urovo.manager")
    override fun onRequestSetAmount() {
        Log.d("PrintValues", "onRequestSetAmount()>>>>>>>>>>")
    }

    override fun onReturnCheckCardResult(
        checkCardResult: ContantPara.CheckCardResult?,
        hashString: Hashtable<String, String>?
    ) {
        dialogProvider.showProgressBar("Processing", "Card Inserted..")
        Log.d("PrintValues", "onReturnCheckCardResult()>>>>>>>>>> $checkCardResult ")
    }

    override fun onRequestSelectApplication(p0: ArrayList<String>?) {
        Log.d("PrintValues", "onReturnCheckCardResult()>>>>>>>>>> $p0 ")
    }

    override fun onRequestPinEntry(pinEntrySource: ContantPara.PinEntrySource?) {
        Log.d("PrintValues", "onRequestPinEntry() online pin entry>>>>>>>>>> $pinEntrySource ")

        if (pinEntrySource == ContantPara.PinEntrySource.KEYPAD) {
            processOnlinePin()
        }
    }

    override fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onRequestConfirmCardno() {
        dialogProvider.showProgressBar("Processing")
        cardData.pan = getCardNumber()
        dialogProvider.confirm(
            activity.getString(R.string.emv_confirm_card_no),
            cardData.pan.mask(6, 4)
        ) {
            onSubmit {
                dialogProvider.showProgressBar(R.string.handling)
                emvNfcKernelApi.sendConfirmCardnoResult(true)
            }

            onClose {
                continuation.resume(cardData.apply {
                    status = CardTransactionStatus.UserCancel
                })
            }
        }
    }

    override fun onRequestFinalConfirm() {
        emvNfcKernelApi.sendFinalConfirmResult(true)
    }

    override fun onRequestOnlineProcess(tlvData: String?, dataKsn: String?) {
        Thread {
            try {
                activity.runOnUiThread {
                    dialogProvider.showProgressBar("Processing")
                }
                getTlvData()
                Thread.sleep(1500)
                if(isOnlinePinEntered) {
                    cardData.status = CardTransactionStatus.Success
                    continuation.resume(cardData)
                } else {
                    val responseDataFromOnlineProcessing = "710F860D842400000817C217D34162474C910A1397ECEFC7A6051100128A023030"
                    emvNfcKernelApi.sendOnlineProcessResult(true, responseDataFromOnlineProcessing)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emvNfcKernelApi.sendOnlineProcessResult(false, "")
            } finally {
                activity.runOnUiThread {
                    dialogProvider.hideProgressBar()
                }
            }
        }.start()

    }

    override fun onReturnBatchData(p0: String?) {
        dialogProvider.showProgressBar("Processing", "onReturnBatchData()")
        Log.d("PrintValues", "onReturnBatchData()>>>>>>> data: $p0")
    }

    override fun onReturnTransactionResult(transactionResult: ContantPara.TransactionResult?) {

        Log.d(
            "PrintValues",
            "onReturnTransactionResult()>>>>>>> transactionResult: $transactionResult"
        )
        when (transactionResult) {
            ContantPara.TransactionResult.OFFLINE_APPROVAL,
            ContantPara.TransactionResult.ONLINE_APPROVAL -> {
                dialogProvider.showProgressBar("Processing")
                Thread.sleep(1500)
                cardData.status = CardTransactionStatus.Success
            }

            ContantPara.TransactionResult.OFFLINE_DECLINED,
            ContantPara.TransactionResult.ONLINE_DECLINED -> {
                cardData.status = CardTransactionStatus.Failure
            }

            ContantPara.TransactionResult.TERMINATED -> {
                cardData.status = CardTransactionStatus.Error
            }

            ContantPara.TransactionResult.ICC_CARD_REMOVED -> {
                cardData.status = CardTransactionStatus.CardRemoved
            }

            ContantPara.TransactionResult.CANCELED_OR_TIMEOUT -> {
                cardData.status = CardTransactionStatus.UserCancel
            }

            ContantPara.TransactionResult.CANCELED -> {
                cardData.status = CardTransactionStatus.UserCancel
            }

            else -> {
                cardData.status = CardTransactionStatus.Failure
            }
        }

        if (!hasResumed) {
            hasResumed = true
            continuation.resume(cardData)
        }
    }

    override fun onRequestDisplayText(p0: ContantPara.DisplayText?) {
        TODO("Not yet implemented")
    }

    override fun onRequestOfflinePINVerify(
        pinEntrySource: ContantPara.PinEntrySource?,
        pinEntryType: Int,
        bundle: Bundle?
    ) {
        dialogProvider.hideProgressBar()

        if (pinEntrySource == ContantPara.PinEntrySource.KEYPAD) {
            val pinTryTimes = emvNfcKernelApi.offlinePinTryTimes
            bundle?.apply {
                putInt("PinTryTimes", pinTryTimes)
                putBoolean("isFirstTime", true)
            }
            if (pinTryTimes == 1) {
                processOfflinePin(pinEntryType, true, bundle!!)
            } else {
                processOfflinePin(pinEntryType, false, bundle!!)
            }
        }
    }

    override fun onReturnIssuerScriptResult(p0: ContantPara.IssuerScriptResult?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onNFCrequestTipsConfirm(p0: ContantPara.NfcTipMessageID?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onReturnNfcCardData(p0: Hashtable<String, String>?) {
        TODO("Not yet implemented")
    }

    override fun onNFCrequestOnline() {
        TODO("Not yet implemented")
    }

    override fun onNFCrequestImportPin(p0: Int, p1: Int, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun onNFCTransResult(p0: ContantPara.NfcTransResult?) {
        TODO("Not yet implemented")
    }

    override fun onNFCErrorInfor(p0: ContantPara.NfcErrMessageID?, p1: String?) {
        TODO("Not yet implemented")
    }

    private fun getCardNumber(): String {
        var cardno = emvNfcKernelApi.getValByTag(0x5A)
        if (cardno.isNullOrEmpty()) {
            cardno = emvNfcKernelApi.getValByTag(0x57)
            if (cardno == null || cardno == "") return ""
            cardno = cardno.substring(0, cardno.uppercase(Locale.getDefault()).indexOf("D"))
        }
        if (cardno[cardno.length - 1] == 'f' || cardno[cardno.length - 1] == 'F' || cardno[cardno.length - 1] == 'd' || cardno[cardno.length - 1] == 'D') cardno =
            cardno.substring(0, cardno.length - 1)
        return cardno
    }

    private fun processOfflinePin(pinEntryType: Int, isLastPinEntry: Boolean, bundle: Bundle): Int {
        isOnlinePinEntered = false
        var ret = 0
        val amount = (sessionData.amount / 100.0).toCurrencyFormat()
        val emvBundle = Bundle()
        val paramVariables = Bundle()
        paramVariables.apply {
            putInt("inputType", 3)
            putInt("CardSlot", 0)
            putBoolean("sound", true)
            putBoolean("onlinePin", false)
            putBoolean("FullScreen", true)
            putLong("timeOutMS", 60000)
            putString("supportPinLen", "0,4")
            putString("title", "Security Keyboard")
            putBoolean("randomKeyboard", true)
        }

        val pinTryTimes = bundle.getInt("PinTryTimes")
        val isFirst = bundle.getBoolean("isFirstTime", false)

        if (isLastPinEntry) {
            if (isFirst) paramVariables.putString("message", "Please input PIN \nLast PIN Try")
            else paramVariables.putString("message", "Please input PIN \nWrong PIN \n Last Pin Try")
        } else {
            if (isFirst) paramVariables.putString("message", "Please input PIN\n$amount")
            else paramVariables.putString(
                "message",
                "Please input PIN \nWrong PIN \nPin Try Times: $pinTryTimes"
            )
        }

        paramVariables.apply {
            putInt("PinTryMode", 1)
            putString("ErrorMessage", "Incorrect PIN, $pinTryTimes More Retries")
            putString("ErrorMessageLast", "Incorrect PIN, Last Chance")
        }

        val seManager = SEManager()
        ret = seManager.getPinBlockEx(paramVariables, object : IInputActionListener.Stub() {
            override fun onInputChanged(type: Int, result: Int, bundle: Bundle?) {
                try {
                    if (type == 2) {

                    } else if (type == 0) {
                        //pin bypass
                        if (result == 0) {
                            Log.d("PrintValues", "Process offline pin bypass")
                            emvNfcKernelApi.sendOfflinePINVerifyResult(1)
                        } else {
                            emvNfcKernelApi.sendOfflinePINVerifyResult(-198)
                        }
                    } else if (type == 3) {
                        //offline plaintext pin
                        if (result == 0) {
                            //offline plaintext verified successfully
                            emvNfcKernelApi.sendOfflinePINVerifyResult(0)
                        } else {
                            //Incorrect pin, try again
                            val argumentString = result.toString() + ""
                            if (argumentString.length >= 4 && "71" == argumentString.subSequence(
                                    0,
                                    2
                                )
                            ) {
                                if ("7101" == argumentString) {
                                    emvNfcKernelApi.sendOfflinePINVerifyResult(-192) //Pin blocked
                                } else {
                                    if ("7102" == argumentString) {
                                        emvBundle.apply {
                                            putBoolean("isFirstTime", false)
                                            putInt("PinTryTimes", 1)
                                        }
                                        processOfflinePin(
                                            pinEntryType,
                                            true,
                                            emvBundle
                                        ) //last pin try
                                    } else {
                                        emvBundle.apply {
                                            putBoolean("isFirstTime", false)
                                            putInt(
                                                "PinTryTimes",
                                                (argumentString.substring(2, 4).toInt() - 1)
                                            )
                                        }
                                        processOfflinePin(pinEntryType, false, emvBundle)
                                    }
                                }
                            } else if ("7074" == argumentString) {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                            } else if ("7072" == argumentString || "7073" == argumentString) {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-202) //IC command failed
                            } else {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-198) //Return code error
                            }
                        }
                    } else if (type == 4) {
                        //offline encryption pin
                        if (result == 0) {
                            emvNfcKernelApi.sendOfflinePINVerifyResult(0)
                        } else {
                            val argumentString = result.toString() + ""
                            if (argumentString.length >= 4 && "71" == argumentString.subSequence(
                                    0,
                                    2
                                )
                            ) {
                                if ("7101" == argumentString) {
                                    emvNfcKernelApi.sendOfflinePINVerifyResult(-192) //Pin blocked
                                } else {
                                    if ("7102" == argumentString) {
                                        emvBundle.apply {
                                            putBoolean("isFirstTime", false)
                                            putInt("PinTryTimes", 1)
                                        }
                                        processOfflinePin(
                                            pinEntryType,
                                            true,
                                            emvBundle
                                        ) //last pin try
                                    } else {
                                        emvBundle.apply {
                                            putBoolean("isFirstTime", false)
                                            putInt(
                                                "PinTryTimes",
                                                (argumentString.substring(2, 4).toInt() - 1)
                                            )
                                        }
                                        processOfflinePin(pinEntryType, false, emvBundle)
                                    }
                                }
                            } else if ("7074" == argumentString) {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                            } else if ("7072" == argumentString || "7073" == argumentString) {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-202) //IC command failed
                            } else {
                                emvNfcKernelApi.sendOfflinePINVerifyResult(-198) //Return code error
                            }
                        }
                    } else if (type == 0x10) {
                        // click Cancel button
                        emvNfcKernelApi.sendOfflinePINVerifyResult(-199) //cancel
                    } else if (type == 0x11) {
                        // pin pad timed out
                        emvNfcKernelApi.sendOfflinePINVerifyResult(-199) //timeout
                    } else {
                        emvNfcKernelApi.sendOfflinePINVerifyResult(-198) //Return code error
                    }


                } catch (e: Exception) {

                }
            }

        })
        if (ret == -3 || ret == -4) {
            emvNfcKernelApi.sendOfflinePINVerifyResult(-198)
        }

        return ret
    }

    private fun processOnlinePin() {
        isOnlinePinEntered = true
        val parameters = Bundle()
        val cardNumber = cardData.pan
        val amount = (sessionData.amount / 100.0).toCurrencyFormat()
        val dukptConfig = sessionData.getDukptConfig?.invoke(cardNumber, sessionData.amount / 100.0)
        val posParameter: PosParameter =
            sessionData.getPosParameter?.invoke(cardNumber, sessionData.amount / 100.0)
                ?: defaultPosParameter
        if (dukptConfig != null) {
            emvNfcKernelApi.abortKernel()
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(UrovoCardData().apply {
                    status = CardTransactionStatus.CardRestrictedDukpt
                })
            }
            return
        } else {
            parameters.putInt("PINKeyNo", INDEX_WORKING_KEY) //INDEX_WORKING_KEY = 10
            val masterKey = posParameter.masterKey.hexBytes
            pinPadProviderImpl.loadMainKey(INDEX_MASTER_KEY, masterKey, null)
            val pinKey = posParameter.pinKey.hexBytes
            pinPadProviderImpl.loadWorkKey(Constant.KeyType.PIN_KEY, INDEX_MASTER_KEY, INDEX_WORKING_KEY, pinKey, null)
            val kcv = ByteArray(8)
            pinPadProviderImpl.calculateDes(Constant.DesMode.ENC, Constant.Algorithm.ECB, Constant.KeyType.PIN_KEY, INDEX_WORKING_KEY, BytesUtil.hexString2Bytes("0000000000000000"), kcv)
        }

        parameters.apply {
            putString("cardNo", cardNumber)
            putBoolean("sound", true)
            putBoolean("onlinePin", true)
            putBoolean("FullScreen", true)
            putLong("timeOutMS", 60000)
            putString("supportPinLen", "0,4") // "4,4
            putString("title", "Security Keyboard")
            putString("message", "Please Enter PIN \n$amount")
            putBoolean("ShowLine", false);
            putShortArray("textSize", shortArrayOf(20, 20, 20, 20, 20, 20, 20))
            putStringArray("numberText", arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
            putString("deleteText", "DELETE")
            putString("cancelText", "CANCEL")
            putString("okText", "OK")
            putBoolean("randomKeyboard", true)
        }

        val adminInputListener: PinInputListener = object : PinInputListener {
            override fun onInput(len: Int, key: Int) {
                Log.d("PrintValues", "key entered: $key")
                Log.d("PrintValues", "key length: $len")
            }

            override fun onConfirm(pinBlock: ByteArray, isNonePin: Boolean) {
                if (isNonePin) {
                    // mKernelApi.bypassPinEntry();//bypass
                    emvNfcKernelApi.bypassPinEntry()
                } else {
                    Log.d("PrintValues", "string value pinblock from the pin pad is: ${String(pinBlock)}" )
                    Log.d("PrintValues", "original pinblock from the pin pad is >>>>>> ${pinBlock.hexString}" )
                    val actualPinBlock = posParameter.pinKey.hexBytes.asDesEdeKey.encrypt(String(pinBlock).hexBytes).copyOf(8)
                    Log.d("PrintValues", "The pinblock for des encryption is ${actualPinBlock.hexString}")
                    cardData.pinBlock = actualPinBlock.hexString
                    emvNfcKernelApi.sendPinEntry()
                }
            }

            override fun onConfirm_dukpt(PinBlock: ByteArray, ksn: ByteArray) {
                if (PinBlock.isEmpty()) {
                    // mKernelApi.bypassPinEntry();//bypass
                    emvNfcKernelApi.ProcOnlinePinAgain()
                } else {
                    Log.d("PrintValues", "pinblock:" + String(PinBlock))
                    Log.d("PrintValues", "ksn:" + String(ksn))
                    emvNfcKernelApi.sendPinEntry()
                }
            }

            override fun onCancel() {
                Log.d("PrintValues", "PINPAD cancel")
                emvNfcKernelApi.cancelPinEntry()
            }

            override fun onTimeOut() {
                emvNfcKernelApi.cancelPinEntry()
            }

            override fun onError(i: Int) {
                emvNfcKernelApi.cancelPinEntry()
            }
        }

        activity.runOnUiThread {
            if (dukptConfig != null) {
                pinPadProviderImpl.GetDukptPinBlock(parameters, adminInputListener)
            } else {
                pinPadProviderImpl.getPinBlockEx(parameters, adminInputListener)
            }
        }
    }

    fun tripleDesDecrypt(pinKey: ByteArray, encryptedPinBlock: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        val secretKeySpec = SecretKeySpec(pinKey, "DESede")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(encryptedPinBlock)
    }


    private fun getTlvData() {
        cardData.mIccString = getField55String()
        cardData.apply {
            track2 = emvNfcKernelApi.getValByTag(0x57)
            var markerIndex = track2.indexOf("D")
            if (markerIndex < 0) {
                markerIndex = track2.indexOf("=")
            }
            src = track2.substring(markerIndex + 5, markerIndex + 8)
            transactionAmount = emvNfcKernelApi.getValByTag(0x9F02)
            exp = emvNfcKernelApi.getValByTag(0x5F24)
            holder = String(emvNfcKernelApi.getValByTag(0x5F20).hexBytes).replace("/", " ")
            cardSequenceNumber = emvNfcKernelApi.getValByTag(0x5F34)
            aid = emvNfcKernelApi.getValByTag(0x9F06)
            cardMethod = CardReaderEvent.CHIP
        }
        val tvr = emvNfcKernelApi.getValByTag(0x95).hexBytes
        if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
            cardData.status = CardTransactionStatus.OfflinePinVerifyError
        }
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
        field55 = emvNfcKernelApi.getTlvByTagLists(tags.toMutableList())
        return field55!!
    }

    companion object {
        const val INDEX_DUKPT_PIN = 3
        const val INDEX_WORKING_KEY = 10
        const val INDEX_MASTER_KEY = 10
    }
}