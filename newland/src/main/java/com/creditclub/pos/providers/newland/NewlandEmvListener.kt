package com.creditclub.pos.providers.newland

import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.mask
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.creditclub.pos.providers.newland.pin.KeyBoardNumberActivity
import com.creditclub.pos.providers.newland.util.EmvL3Configuration
import com.creditclub.pos.providers.newland.util.Singletons
import com.newland.nsdk.core.api.internal.emvl2.type.publickey
import com.newland.sdk.emvl3.api.common.EmvL3Const
import com.newland.sdk.emvl3.api.common.ErrorCode
import com.newland.sdk.emvl3.api.common.listener.Candidate
import com.newland.sdk.emvl3.api.internal.EmvL3
import com.newland.sdk.emvl3.api.internal.listener.CompleteTransactionListener
import com.newland.sdk.emvl3.api.internal.listener.PerfromTransactionListener
import com.nlutils.emv.EmvTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jpos.iso.ISOUtil
import org.koin.core.component.KoinComponent
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface KeyboardNumberActivityCallback {
    fun onPinEntered(errorCode: Int, pinBlock: ByteArray?, ksn: ByteArray?, isCancelled : Boolean, isTimedOut: Boolean, isAnError: Boolean)
}
object KeyboardNumberActivityCallbackHolder {
    var callback: KeyboardNumberActivityCallback? = null
}

class NewlandEmvListener(
    private val emvL3: EmvL3,
    private val activity: CreditClubActivity,
    private val continuation: Continuation<CardData?>,
) : PerfromTransactionListener, KoinComponent, KeyboardNumberActivityCallback{
    private val dialogProvider = activity.dialogProvider
    private val cardData = NewlandCardData()
    private var ioScope = CoroutineScope(Dispatchers.IO)
    private var isOfflinePin: Boolean = false
    private var endTransactionStatus : CardTransactionStatus = CardTransactionStatus.UserCancel
    override fun uiEvent(uiEvent: Int, uiEventData: ByteArray?) {
        Log.d("NewLandUIEvent", "The ui event : $uiEvent")
        Log.d("NewLandUIEvent", "The ui event data: ${uiEventData.contentToString()}")
        ioScope.launch {
            uiEventData?.let { processUIEvent(uiEvent, it) }
        }
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun getApduData(p0: Int, p1: ByteArray?, p2: Int, p3: ByteArray?, p4: Int) {
        Log.d("NewLandUIEvent", "We entered here for insert card for getApduData : $p0 ${p1.contentToString()} $p2 ${p3.contentToString()}")
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun transResult(result: Int, errorCode: Int) {
        Log.d("NewLandUIEvent", "We entered here for insert card for transResult")
        Log.d("NewLandUIEvent", "Result is : $result")
        Log.d("NewLandUIEvent", "ErrorCode is : $errorCode")
        activity.runOnUiThread {
            dialogProvider.showProgressBar("PROCESSING..")
            when (result) {
                EmvL3Const.TransResult.L3_TXN_APPROVED -> {
                    dialogProvider.hideProgressBar()
                    getEmvData(false)
                    emvL3.terminateTransaction()
                    continuation.resume(cardData.apply {
                        status = CardTransactionStatus.OfflinePinVerifyError
                    })
                }

                EmvL3Const.TransResult.L3_TXN_DECLINE -> {
                    dialogProvider.hideProgressBar()
                    getEmvData(false)
                    emvL3.terminateTransaction()
                    continuation.resume(cardData.apply {
                        status = CardTransactionStatus.OfflinePinVerifyError
                    })
                }

                EmvL3Const.TransResult.L3_TXN_ONLINE -> {
                    getEmvData(false)
                    goOnline()
                }

                else -> {
                    endTransactionStatus = if (result == EmvL3Const.TransResult.L3_TXN_TRY_ANOTHER) {
                        CardTransactionStatus.UserCancel
                    } else if (errorCode == ErrorCode.L3_ERR_TIMEOUT) {
                        CardTransactionStatus.Timeout
                    } else if (errorCode == ErrorCode.L3_ERR_CANCEL || errorCode == ErrorCode.EMV_ERR_CANCEL) {
                        CardTransactionStatus.UserCancel
                    } else {
                        CardTransactionStatus.OfflinePinVerifyError
                    }
                    dialogProvider.hideProgressBar()
                    emvL3.terminateTransaction()
                    continuation.resume(cardData.apply {
                        status = endTransactionStatus
                    })
                }
            }
        }
    }

    private fun getEmvData(failure: Boolean){
        cardData.apply {
            ret = CardTransactionStatus.Success.code
            transactionAmount = emvL3.getData(EmvTag.TAG_9F02_TM_AUTHAMNTN).hexString
            exp = emvL3.getData(EmvTag.TAG_5F24_IC_APPEXPIREDATE).hexString.dropLast(2)
            holder = String(emvL3.getData(EmvTag.TAG_5F20_IC_HOLDERNAME).hexString.hexBytes)
            cardSequenceNumber = emvL3.getData(EmvTag.TAG_5F34_IC_PANSN).hexString
            aid = emvL3.getData(EmvTag.TAG_9F06_TM_AID).hexString
            track2 = emvL3.getData(EmvTag.TAG_57_IC_TRACK2EQUDATA).hexString
            var markerIndex = track2.indexOf("D")
            if(markerIndex < 0){
                markerIndex = track2.indexOf("=")
            }
            src = track2.substring(markerIndex + 5, markerIndex + 8)
            atc = emvL3.getData(EmvTag.TAG_9F36_IC_ATC).hexString
            terminalCapabilities = emvL3.getData(EmvTag.TAG_9F33_TM_CAP).hexString
            terminalType = emvL3.getData(EmvTag.TAG_9F35_TM_TERMTYPE).hexString
            iad = emvL3.getData(EmvTag.TAG_9F10_IC_ISSAPPDATA).hexString
            tvr = emvL3.getData(EmvTag.TAG_95_TM_TVR).hexString
            unpredictedNumber = emvL3.getData(EmvTag.TAG_9F37_TM_UNPNUM).hexString
            dedicatedFileName = emvL3.getData(EmvTag.TAG_84_IC_DFNAME).hexString
            transactionDate = emvL3.getData(EmvTag.TAG_9A_TM_TRANSDATE).hexString
            transactionType = emvL3.getData(EmvTag.TAG_9C_TM_TRANSTYPE).hexString
            transactionCurrency = emvL3.getData(EmvTag.TAG_5F2A_TM_CURCODE).hexString
            cardHolderVerificationMethod = emvL3.getData(EmvTag.TAG_9F34_TM_CVMRESULT).hexString
            amountAuthorized = emvL3.getData(EmvTag.TAG_9F02_TM_AUTHAMNTN).hexString
            amountOther = emvL3.getData(EmvTag.TAG_9F03_TM_OTHERAMNTN).hexString
            aip = emvL3.getData(EmvTag.TAG_82_IC_AIP).hexString
            if(!failure){
                cryptogram = emvL3.getData(EmvTag.TAG_9F26_IC_AC).hexString
                cryptogramInformationData = emvL3.getData(EmvTag.TAG_9F27_IC_CID).hexString
            }
            Log.d("NewLandUIEvent", "Here is the card data : $this")
        }
        val tvr = emvL3.getData(EmvTag.TAG_95_TM_TVR).hexString.hexBytes
        if (tvr.isNotEmpty() && tvr.last() == '1'.code.toByte()) {
            cardData.status = CardTransactionStatus.OfflinePinVerifyError
        }
    }

    private fun goOnline(){
        Log.d("NewLandUIEvent", "GoOnline() called")
        var emvHostData: ByteArray? = ByteArray(256)
        // Add host response data (tags 8A, 91, scripts 71 & 72)
        // Add host response data (tags 8A, 91, scripts 71 & 72)
        emvHostData = EmvL3Configuration.hexStringToByteArray("8A023030")
        emvL3.completeTransaction(true, emvHostData, object : CompleteTransactionListener{
            override fun uiEvent(uiEvent: Int, uiEventData: ByteArray?) {
                ioScope.launch {
                    uiEventData?.let { processUIEvent(uiEvent, it) }
                }
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
            }

            override fun getApduData(p0: Int, p1: ByteArray?, p2: Int, p3: ByteArray?, p4: Int) {
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
            }

            override fun transResult(result: Int, errorCode: Int) {
                Log.d("NewLandUIEvent", "End result : $result")
                when(result){
                    EmvL3Const.TransResult.L3_TXN_APPROVED -> {
                        dialogProvider.hideProgressBar()
                        cardData.status = CardTransactionStatus.Success
                        emvL3.terminateTransaction()
                        Singletons.clearEmvL3()
                        Singletons.clearNsdkModuleManager()
                        continuation.resume(cardData)
                    }

                }
            }

            override fun voiceReferrals() {
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
            }

        })
    }

    override fun getPIN(pinType: Int, pinTryCount: Int, publickey: publickey?) {
        Log.d("NewLandUIEvent", "We entered here for insert card for getPIN")

        /**
         * To notice the customer to enter card pin,and get the pin from the UI.
         * Card pin includes 3 types, offline plain pin,offline cipher pin and online pin.
         * Offline cipher pin needs public key to encrypt pin.
         */
        isOfflinePin = when (pinType) {
            EmvL3Const.PINType.PIN_OFFLINE -> true
            EmvL3Const.PINType.PIN_OFFLINE_ENCIPHERED -> {
                if (pinType == EmvL3Const.PINType.PIN_OFFLINE_ENCIPHERED && publickey == null) {
                    emvL3.responseEvent(ErrorCode.L3_ERR_FAIL, null)
                }
                true
            }
            else -> false
        }
        Log.d("OnPinEntered", "Is it an offline pin: $isOfflinePin")
        Log.d("OnPinEntered", "The pin try count is: $pinTryCount")
        startEnterPin(pinType, publickey)
    }

    private fun startEnterPin(pinType: Int, publicKey: publickey?) {
        val intent = Intent(this@NewlandEmvListener.activity, KeyBoardNumberActivity::class.java)
        KeyboardNumberActivityCallbackHolder.callback = this
        intent.apply {
            putExtra("pinType", pinType)
            putExtra("pan", cardData.pan)
            if(pinType != 0){
                putExtra("module", ISOUtil.hexString(publicKey?.pk_modulus))
                putExtra("exponent", ISOUtil.hexString(publicKey?.pk_exponent))
            }
        }
        startActivityForResult(activity, intent, 1, null)
    }

    override fun selectCandidateList(candidateList: ArrayList<Candidate>?) {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun selectAccount() {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun selectLanguage(p0: ByteArray?) {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun checkCredentials(p0: Byte, p1: ByteArray?) {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun dek_det(p0: Int, p1: ByteArray?) {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun onFinalSelect(cardInterface: Int, aid: ByteArray?) {
        Log.d("NewLandUIEvent", "We entered here for insert card for onFinalSelect, $cardInterface and ${aid?.hexString.toString()}")
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun getManualData() {
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun confirmPAN(cardPan: String?) {
        dialogProvider.hideProgressBar()
        cardData.pan = cardPan!!
        activity.runOnUiThread {
            dialogProvider.confirm(
                "Confirm Card Pan",
                cardData.pan.mask(6,4)
            ){
                onSubmit {
                    dialogProvider.showProgressBar("Processing..")
                    emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
                }
                onClose {
                    continuation.resume(cardData.apply {
                        status = CardTransactionStatus.UserCancel
                    })
                }
            }
        }
    }

    private fun processUIEvent(uiEvent: Int, uiEventData: ByteArray) {
        when (uiEvent) {
            EmvL3Const.UIEvent.UI_PRESENT_CARD -> {
                //Present card again.
                if (uiEventData[0].toInt() == EmvL3Const.UICard.UI_PRESENTCARD_AGAIN) {
                    activity.runOnUiThread {
                        dialogProvider.showError("PRESENT CARD AGAIN")
                    }

                }

                //Contact card fallback,prompting customer to swipe card
                if (uiEventData[0].toInt() == EmvL3Const.UICard.UI_FALLBACK_CT) {
                    activity.runOnUiThread {
                        dialogProvider.showError("CONTACT FALLBACK")
                    }
                }

                //Contactless card fallback,prompting customer to insert or swipe card
                if (uiEventData[0].toInt() == EmvL3Const.UICard.UI_FALLBACK_CLSS) {
                    activity.runOnUiThread {
                        dialogProvider.showError("CONTACTLESS FALLBACK")
                    }
                }

                //Swiped card which has chip,need to insert the card
                if (uiEventData[0].toInt() == EmvL3Const.UICard.UI_USE_CHIP) {
                    activity.runOnUiThread {
                        dialogProvider.showError("PLEASE USE CHIP")
                    }
                }

                //To show the current card interface type, like EmvL3Const.UICard.UI_STRIPE{@}
                if (uiEventData[0].toInt() != EmvL3Const.UICard.UI_KEYIN) {
                    activity.runOnUiThread {
                        dialogProvider.showProgressBar("PROCESSING CARD...")
                    }
                }
            }

            EmvL3Const.UIEvent.UI_PROCESSING -> {
                uiEventData.let { uiData ->
                    if(uiData.isEmpty() && uiData == null) {
                        activity.runOnUiThread {
                            dialogProvider.showProgressBar("PROCESSING...")
                        }
                    }
                }
            }

            EmvL3Const.UIEvent.UI_PIN_STATUS -> {
                activity.runOnUiThread {
                    dialogProvider.showProgressBar("PIN STATUS")
                }
            }
        }
    }

    override fun onPinEntered(errorCode: Int, pinBlock: ByteArray?, ksn: ByteArray?, isCancelled: Boolean, isTimedOut: Boolean, isAnError: Boolean) {
        Log.d("OnPinEntered", "The errorCode is : $errorCode")
        Log.d("OnPinEntered", "The pinblock is : ${pinBlock?.hexString}")
        Log.d("OnPinEntered", "The ksn is : $ksn")
        Log.d("OnPinEntered", "The cancel is : $isCancelled")
        Log.d("OnPinEntered", "The timed out is : $isTimedOut")
        Log.d("OnPinEntered", "The error is : $isAnError")

        if(isCancelled && errorCode == -502){
            dialogProvider.hideProgressBar()
            emvL3.terminateTransaction()
            continuation.resume(cardData.apply {
                status = CardTransactionStatus.UserCancel
            })
        }
        if(isTimedOut && errorCode == -503){
            dialogProvider.hideProgressBar()
            emvL3.terminateTransaction()
            continuation.resume(cardData.apply {
                status = CardTransactionStatus.Timeout
            })
        }
        if(isAnError && errorCode == -501){
            dialogProvider.hideProgressBar()
            emvL3.terminateTransaction()
            continuation.resume(cardData.apply {
                status = CardTransactionStatus.Error
            })
        }
        if(!isCancelled && !isTimedOut && !isAnError) {
            if (isOfflinePin) {
                cardData.ksnData = ""
                cardData.pinBlock = ""
            }

            if (!isOfflinePin && pinBlock!!.isNotEmpty()) {
                cardData.pinBlock = pinBlock.hexString
                cardData.ksnData = "0000000000000000"
            }
            emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, pinBlock?.hexString?.hexBytes)
        }
    }
}