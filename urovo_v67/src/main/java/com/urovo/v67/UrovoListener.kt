package com.urovo.v67

import android.os.Bundle
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import org.koin.core.component.KoinComponent
import java.util.ArrayList
import java.util.Hashtable
import kotlin.coroutines.Continuation

class UrovoListener(
    private val activity: CreditClubActivity,
    private val emvNfcKernelApi: EmvNfcKernelApi,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>
) : EmvListener, KoinComponent {
    private val dialogProvider = activity.dialogProvider
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
        TODO("Not yet implemented")
    }

    override fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onRequestConfirmCardno() {
        dialogProvider.showProgressBar("Processing")
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
        TODO("Not yet implemented")
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
}