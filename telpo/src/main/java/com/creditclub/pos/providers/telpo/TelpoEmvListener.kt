package com.creditclub.pos.providers.telpo

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.core.content.edit
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.format
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.utils.TripleDesCipher
import com.telpo.emv.*
import com.telpo.emv.util.getPinTextInfo
import com.telpo.emv.util.getValue
import com.telpo.emv.util.hexString
import com.telpo.emv.util.xor
import com.telpo.pinpad.PinParam
import com.telpo.pinpad.PinpadService
import org.koin.core.KoinComponent
import org.koin.core.get
import java.time.Instant
import java.io.UnsupportedEncodingException


class TelpoEmvListener(
    private val context: Context,
    private val emvService: EmvService,
    private val sessionData: PosManager.SessionData
) : EmvServiceListener(), KoinComponent {
    internal var ksnData: String? = null
    internal var cardData: TelpoEmvCardData? = null
    internal var pinBlock: String? = null
    private var bUIThreadisRunning = true
    private val prefs = context.getSharedPreferences("TelpoPosManager", 0)

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
        debugOnly {
            Log.w("input pin", "onInputPin: " + "callback [onInputPIN]:" + PinData.type)
        }
        bUIThreadisRunning = true
        var result = 0
        Thread {
            val amount = sessionData.amount / 100.0
            val param = PinParam(context).apply {
                CardNo = emvService.getValue(0x5A, hex = false, padded = true)
                PinBlockFormat = 0
                KeyIndex = 0
                WaitSec = 60
                MaxPinLen = 6
                MinPinLen = 4
                IsShowCardNo = 0
                Amount = amount.toCurrencyFormat()
            }
            val dukptConfig = sessionData.getDukptConfig?.invoke(param.CardNo, amount)
            PinpadService.Open(context)
            wakeUpAndUnlock(context)
            if (dukptConfig != null) {
                param.KeyIndex = 3
                val paddedIpek = dukptConfig.ipek.padStart(20, '0')
                val paddedKsn = dukptConfig.ksn.padStart(20, '0')
                val xorValue = paddedIpek.hexBytes xor paddedKsn.hexBytes
                val kcv = xorValue.hexString.takeLast(6)
                if (prefs.getString("kcv", null) != kcv) {
                    PinpadService.TP_PinpadWriteDukptIPEK(
                        dukptConfig.ipek.hexBytes,
                        dukptConfig.ksn.hexBytes,
                        0,
                        PinpadService.KEY_WRITE_DIRECT,
                        0
                    )
                    prefs.edit { putString("kcv", kcv) }
                }
                PinpadService.TP_PinpadDukptSessionStart(0)
            }
            val ret: Int
            val pinText = PinData.getPinTextInfo(param)
            if (PinData.type.toInt() == 0) {
                if (dukptConfig == null) {
                    ret = PinpadService.TP_PinpadGetPin(param)
                } else {
                    ret = PinpadService.TP_PinpadDukptGetPin(param)
                    ksnData = param.Curr_KSN.hexString
                }
                pinBlock = param.Pin_Block.hexString
            } else {
                if (dukptConfig == null) {
                    ret = PinpadService.TP_PinpadGetPlainPin(param, 0, 0, 0)
                    if (ret == PinpadService.PIN_OK) {
                        pinBlock = param.Pin_Block.encryptedPinBlock.hexString
                        PinData.Pin = param.Pin_Block
                    }
                } else {
                    ret = PinpadService.TP_PinpadDukptGetPin(param)
                    if (ret == PinpadService.PIN_OK) {
                        PinData.type = 0
                        pinBlock = param.Pin_Block.hexString
                        ksnData = param.Curr_KSN.hexString
//                        PinData.Pin = ByteArray(4) { it.toByte() }
                    }
                }
            }
            result = when {
                ret == PinpadService.PIN_ERROR_CANCEL -> EmvService.ERR_USERCANCEL
                ret == PinpadService.PIN_OK && param.Pin_Block.hexString == "00000000" -> {
                    EmvService.ERR_NOPIN
                }
                ret == PinpadService.PIN_OK -> EmvService.EMV_TRUE
                ret == PinpadService.PIN_ERROR_TIMEOUT -> EmvService.ERR_TIMEOUT
                else -> EmvService.EMV_FALSE
            }

            if (dukptConfig != null) PinpadService.TP_PinpadDukptSessionEnd()
            bUIThreadisRunning = false
        }.start()

        while (bUIThreadisRunning) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        debugOnly {
            Log.w("listener", "onInputPIN callback result: $result")
        }
        return result
    }

    override fun onMir_DataExchange(): Int {
        TODO("Not yet implemented")
    }

    override fun onSelectApp(appList: Array<out EmvCandidateApp>): Int {
        //return appList[0].index.toInt()
        return appList[0].index.toInt()
    }

    override fun onMir_FinishReadAppData(): Int {
        TODO("Not yet implemented")
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

        val ret = if (pinBlock.isNullOrBlank()) {
            onInputPin(EmvPinData())
        } else EmvService.EMV_TRUE
        cardData = TelpoEmvCardData(emvService)
        cardData?.ksnData = ksnData
        return ret
    }

    override fun onRequireTagValue(tag: Int, len: Int, value: ByteArray?): Int {
        return EmvService.EMV_TRUE
    }

    override fun onRequireDatetime(datetime: ByteArray): Int {
        return try {
            val str = Instant.now().format("yyyyMMddHHmmss")
            val time: ByteArray
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

    override fun onMir_Hint(): Int {
        TODO("Not yet implemented")
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

    private inline val ByteArray.encryptedPinBlock: ByteArray
        get() {
            val pin = String(this)
            val pan = emvService.getValue(0x5A, hex = false, padded = true)
            val pinBlock = "0${pin.length}$pin".padEnd(16, 'F')
            val panBlock = pan.substring(3, pan.lastIndex).padStart(16, '0')
            val cipherKey = get<PosParameter>().pinKey.hexBytes
            val cryptData = pinBlock.hexBytes xor panBlock.hexBytes
            val tripleDesCipher = TripleDesCipher(cipherKey)
            return tripleDesCipher.encrypt(cryptData).copyOf(8)
        }
}