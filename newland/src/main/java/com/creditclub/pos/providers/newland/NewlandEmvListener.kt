package com.creditclub.pos.providers.newland

import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.newland.nsdk.core.api.internal.emvl2.type.publickey
import com.newland.sdk.emvl3.api.common.listener.Candidate
import com.newland.sdk.emvl3.api.internal.EmvL3
import com.newland.sdk.emvl3.api.internal.listener.PerfromTransactionListener
import org.koin.core.component.KoinComponent
import java.util.ArrayList
import kotlin.coroutines.Continuation

class NewlandEmvListener(
    private val emvL3: EmvL3,
    private val ordaActivity: CreditClubActivity,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>,
) : PerfromTransactionListener, KoinComponent{
    override fun uiEvent(p0: Int, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun getApduData(p0: Int, p1: ByteArray?, p2: Int, p3: ByteArray?, p4: Int) {
        TODO("Not yet implemented")
    }

    override fun transResult(p0: Int, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun getPIN(p0: Int, p1: Int, p2: publickey?) {
        TODO("Not yet implemented")
    }

    override fun selectCandidateList(p0: ArrayList<Candidate>?) {
        TODO("Not yet implemented")
    }

    override fun selectAccount() {
        TODO("Not yet implemented")
    }

    override fun selectLanguage(p0: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun checkCredentials(p0: Byte, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun dek_det(p0: Int, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onFinalSelect(p0: Int, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun getManualData() {
        TODO("Not yet implemented")
    }

    override fun confirmPAN(p0: String?) {
        TODO("Not yet implemented")
    }
}