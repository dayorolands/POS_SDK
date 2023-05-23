package com.creditclub.pos.providers.newland

import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.mask
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexString
import com.newland.nsdk.core.api.internal.emvl2.type.publickey
import com.newland.sdk.emvl3.api.common.EmvL3Const
import com.newland.sdk.emvl3.api.common.ErrorCode
import com.newland.sdk.emvl3.api.common.listener.Candidate
import com.newland.sdk.emvl3.api.internal.EmvL3
import com.newland.sdk.emvl3.api.internal.listener.PerfromTransactionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class NewlandEmvListener(
    private val emvL3: EmvL3,
    private val activity: CreditClubActivity,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>,
) : PerfromTransactionListener, KoinComponent{
    private val dialogProvider = activity.dialogProvider
    private val cardData = NewlandCardData()
    private var ioScope = CoroutineScope(Dispatchers.IO)
    private var mainScope = CoroutineScope(Dispatchers.Main)
    override fun uiEvent(uiEvent: Int, uiEventData: ByteArray?) {
        Log.d("NewLandUIEvent", "The ui event : $uiEvent")
        Log.d("NewLandUIEvent", "The ui event data: ${uiEventData.contentToString()}")
        ioScope.launch {
            processUIEvent(uiEvent, uiEventData!!)
        }
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun getApduData(p0: Int, p1: ByteArray?, p2: Int, p3: ByteArray?, p4: Int) {
        Log.d("NewLandUIEvent", "We entered here for insert card for getApduData : $p0 ${p1.contentToString()} $p2 ${p3.contentToString()}")
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun transResult(p0: Int, p1: Int) {
        Log.d("NewLandUIEvent", "We entered here for insert card for transResult")
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun getPIN(p0: Int, p1: Int, p2: publickey?) {
        Log.d("NewLandUIEvent", "We entered here for insert card for getPIN")
        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null)
    }

    override fun selectCandidateList(candidateList: ArrayList<Candidate>?) {
        Log.d("NewLandUIEvent", "We entered here for insert card for selectCandidateList")
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
        Log.d("NewLandUIEvent", "We entered here for insert card for Confirm_PAN")
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
                    if(uiData == null || uiData.isEmpty()) {
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
}