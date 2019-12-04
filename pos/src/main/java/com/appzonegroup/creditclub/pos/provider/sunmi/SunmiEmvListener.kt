package com.appzonegroup.creditclub.pos.provider.sunmi

import android.os.RemoteException
import com.appzonegroup.creditclub.pos.provider.sunmi.utils.LogUtil
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class SunmiEmvListener /*: EMVListenerV2.Stub()*/ {

//    @Throws(RemoteException::class)
//    override fun onWaitAppSelect(
//        appNameList: List<EMVCandidateV2?>?,
//        isFirstSelect: Boolean
//    ) {
//        LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:$isFirstSelect")
//        mProcessStep = EMV_APP_SELECT
//        val candidateNames: Array<String> = getCandidateNames(appNameList)
//        mHandler.obtainMessage(EMV_APP_SELECT, candidateNames)
//            .sendToTarget()
//    }
//
//    companion object {
//        private const val EMV_APP_SELECT = 1
//        private val EMV_FINAL_APP_SELECT = 2
//        private val EMV_CONFIRM_CARD_NO = 3
//        private val EMV_CERT_VERIFY = 4
//        private val EMV_SHOW_PIN_PAD = 5
//        private val EMV_ONLINE_PROCESS = 6
//        private val EMV_SIGNATURE = 7
//        private val EMV_TRANS_SUCCESS = 888
//        private val EMV_TRANS_FAIL = 999
//
//        private val PIN_CLICK_NUMBER = 50
//        private val PIN_CLICK_PIN = 51
//        private val PIN_CLICK_CONFIRM = 52
//        private val PIN_CLICK_CANCEL = 53
//        private val PIN_ERROR = 54
//    }
}