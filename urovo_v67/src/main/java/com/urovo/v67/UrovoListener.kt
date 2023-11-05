package com.urovo.v67

import android.os.Bundle
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.mask
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardTransactionStatus
import com.nexgo.R
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import org.koin.core.component.KoinComponent
import java.util.Hashtable
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class UrovoListener(
    private val activity: CreditClubActivity,
    private val emvNfcKernelApi: EmvNfcKernelApi,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>
) : EmvListener, KoinComponent {
    private val dialogProvider = activity.dialogProvider
    private val cardData = UrovoCardData()
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

    override fun onRequestPinEntry(p0: ContantPara.PinEntrySource?) {
        Log.d("PrintValues", "onRequestPinEntry()>>>>>>>>>> $p0 ")
    }

    override fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onRequestConfirmCardno() {
        dialogProvider.showProgressBar("Processing")
        Log.d("PrintValues", "onRequestConfirmCardNo()>>>>>>>>>> ${getCardNumber()} ")
        cardData.pan = getCardNumber()
        dialogProvider.confirm(
            activity.getString(R.string.emv_confirm_card_no),
            cardData.pan.mask(6,4)
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
        TODO("Not yet implemented")
    }

    override fun onRequestOnlineProcess(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onReturnBatchData(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onReturnTransactionResult(p0: ContantPara.TransactionResult?) {
        TODO("Not yet implemented")
    }

    override fun onRequestDisplayText(p0: ContantPara.DisplayText?) {
        TODO("Not yet implemented")
    }

    override fun onRequestOfflinePINVerify(p0: ContantPara.PinEntrySource?, p1: Int, p2: Bundle?) {
        dialogProvider.showProgressBar("Enter pin here...")
        Log.d("PrintValues", "onRequestOfflinePINVerify()>>>>>>>>>> $p0 , $p1 , $p2 ")
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
}