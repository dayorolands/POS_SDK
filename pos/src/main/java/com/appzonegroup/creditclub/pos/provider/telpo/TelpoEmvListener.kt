package com.appzonegroup.creditclub.pos.provider.telpo

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.appzonegroup.creditclub.pos.card.PosManager
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.telpo.emv.*
import com.telpo.emv.util.StringUtil
import com.telpo.pinpad.PinParam
import com.telpo.pinpad.PinpadService
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*


class TelpoEmvListener(
    private val context: Context,
    val emvService: EmvService,
    private val sessionData: PosManager.SessionData
) : EmvServiceListener() {
    var pinBlock: String? = null
    var mResult: Int = 0
    private var bUIThreadisRunning = true

    private fun wakeUpAndUnlock(context: Context) {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val kl = km.newKeyguardLock("unLock")
        kl.disableKeyguard()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl =
            pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                "pre:bright"
            )
        wl.acquire(10 * 60 * 1000L /*10 minutes*/)
        wl.release()
    }

    override fun onInputPin(PinData: EmvPinData): Int {
        Log.w("input pin", "onInputPin: " + "callback [onInputPIN]:" + PinData.type)
        bUIThreadisRunning = true

        Thread(Runnable {
            var ret: Int
            val param = PinParam(context)
            val pan = EmvTLV(0x5A)

            ret = emvService.Emv_GetTLV(pan)
            if (ret == EmvService.EMV_TRUE) {
                val p = StringBuffer(StringUtil.bytesToHexString(pan.Value))
                if (p[p.toString().length - 1] == 'F') {
                    p.deleteCharAt(p.toString().length - 1)
                }
                param.CardNo = p.toString()
                Log.w("listener", "CardNo: " + param.CardNo)
                log("PAN: " + param.CardNo)
            }

            param.KeyIndex = 0
            param.WaitSec = 60
            param.MaxPinLen = 4
            param.MinPinLen = 4
            param.IsShowCardNo = 0
            param.Amount = CurrencyFormatter.format("${sessionData.amount}")
            PinpadService.Open(context)
            wakeUpAndUnlock(context)
            ret = PinpadService.TP_PinpadGetPin(param)
            pinBlock = StringUtil.bytesToHexString(param.Pin_Block)
            log("TP_PinpadGetPin: " + ret + "\nPinblock: " + StringUtil.bytesToHexString(param.Pin_Block))
            if (ret == PinpadService.PIN_ERROR_CANCEL) {
                mResult = EmvService.ERR_USERCANCEL
                log("get pin : user cancel")
            } else if (ret == PinpadService.PIN_OK && StringUtil.bytesToHexString(param.Pin_Block) == "00000000") {
                mResult = EmvService.ERR_NOPIN
                log("get pin : no pin")
            } else if (ret == PinpadService.PIN_OK) {
                mResult = EmvService.EMV_TRUE
                log("get pin success: " + StringUtil.bytesToHexString(param.Pin_Block))
            } else if (ret == PinpadService.PIN_ERROR_TIMEOUT) {
                mResult = EmvService.ERR_TIMEOUT
                log("get pin : timeout")
            } else {
                mResult = EmvService.EMV_FALSE
                log("get pin error: $ret")
            }

            bUIThreadisRunning = false
        }).start()

        while (bUIThreadisRunning) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        Log.w("listener", "onInputPIN callback result: $mResult")
        return if (mResult != EmvService.EMV_TRUE) {
            mResult
        } else EmvService.EMV_TRUE
    }

    override fun onSelectApp(appList: Array<out EmvCandidateApp>): Int {
        //return appList[0].index.toInt()
        return appList[0].index.toInt()
    }

    override fun onSelectAppFail(ErrCode: Int): Int {
        return EmvService.EMV_TRUE
    }

    override fun onFinishReadAppData(): Int {
        return EmvService.EMV_TRUE
    }

    override fun onVerifyCert(): Int {
        return EmvService.EMV_TRUE
    }

    override fun onOnlineProcess(OnlineData: EmvOnlineData): Int {
        try {
            OnlineData.ResponeCode = "00".toByteArray(charset("ascii"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }


        //paywave
        //-------------------------------------------------------------------------------------------------------
        val param = PinParam(context)

        var pan = EmvTLV(0x5A)
        var ret = emvService.Emv_GetTLV(pan)
        if (ret == EmvService.EMV_TRUE) {
            val p = StringBuffer(StringUtil.bytesToHexString(pan.Value))
            if (p[p.toString().length - 1] == 'F') {
                p.deleteCharAt(p.toString().length - 1)
            }
            param.CardNo = p.toString()
            Log.w("listener", "CardNo: " + param.CardNo)
        } else {
            pan = EmvTLV(0x57)
            if (emvService.Emv_GetTLV(pan) == EmvService.EMV_TRUE) {
                val panstr = StringUtil.bytesToHexString(pan.Value)
                Log.w("pan", "panstr: $panstr")
                val index = panstr.indexOf("D")
                Log.w("pan", "index: $index")
                param.CardNo = panstr.substring(0, index)
            }
        }
        log("PAN: " + param.CardNo)
        //paywave
        //-------------------------------------------------------------------------------------------------------

        return EmvService.EMV_TRUE

    }

    override fun onRequireTagValue(tag: Int, len: Int, value: ByteArray?): Int {
        //paypass——————————-----------------------------------

        Log.e("yw", "onRequireTagValue:")
        val param = PinParam(context)
        val ret: Int
        var pan = EmvTLV(0x5A)
        ret = emvService.Emv_GetTLV(pan)
        if (ret == EmvService.EMV_TRUE) {
            val p = StringBuffer(StringUtil.bytesToHexString(pan.Value))
            if (p[p.toString().length - 1] == 'F') {
                p.deleteCharAt(p.toString().length - 1)
            }
            param.CardNo = p.toString()
            Log.w("listener", "CardNo: " + param.CardNo)
        } else {
            pan = EmvTLV(0x57)
            if (emvService.Emv_GetTLV(pan) == EmvService.EMV_TRUE) {
                val panstr = StringUtil.bytesToHexString(pan.Value)
                Log.w("pan", "panstr: $panstr")
                val index = panstr.indexOf("D")
                Log.w("pan", "index: $index")
                param.CardNo = panstr.substring(0, index)
            }
        }
        log("PAN: " + param.CardNo)


        return EmvService.EMV_TRUE

    }

    override fun onRequireDatetime(datetime: ByteArray): Int {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        val time: ByteArray
        return try {
            time = str.toByteArray(charset("ascii"))
            System.arraycopy(time, 0, datetime, 0, datetime.size)
            EmvService.EMV_TRUE
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.e("MyEmvService", "onRequireDatetime failed")
            EmvService.EMV_FALSE
        }
    }

    override fun onReferProc(): Int {
        return EmvService.EMV_TRUE
    }

    override fun OnCheckException(PAN: String?): Int {
        return EmvService.EMV_FALSE
    }

    override fun OnCheckException_qvsdc(index: Int, PAN: String?): Int {
        return EmvService.EMV_TRUE
    }

    override fun onInputAmount(AmountData: EmvAmountData): Int {
        AmountData.TransCurrCode = 566.toShort()
        AmountData.ReferCurrCode = 566.toShort()
        AmountData.Amount = sessionData.amount
        AmountData.ReferCurrExp = 2.toByte()
        AmountData.ReferCurrCon = 0
        return EmvService.EMV_TRUE
    }

    private fun log(msg: String) {
        Log.d("MyEMV", msg)
    }
}