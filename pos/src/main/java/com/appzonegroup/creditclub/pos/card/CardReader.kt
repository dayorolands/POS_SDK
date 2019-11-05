package com.appzonegroup.creditclub.pos.card

import android.app.Dialog
import android.util.Log
import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.command.WakeUpAndUnlock
import com.telpo.emv.EmvParam
import com.telpo.emv.EmvService
import com.telpo.emv.util.StringUtil
import com.telpo.pinpad.PinParam
import com.telpo.pinpad.PinpadService
import kotlinx.coroutines.*

typealias CardReaderEventListener = (CardReaderEvent) -> Unit
typealias CardDataListener = (CardData?) -> Unit

class CardReader(private val flow: PosActivity, private val emvServiceListener: CustomEmvServiceListener) {
    private val emvService: EmvService = emvServiceListener.emvService
    private var readJob: Job? = null
    private var watchJob: Job? = null

    internal var data = arrayOfNulls<String>(3)

    internal var event: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false

    private var isSessionOver = false

    private var isSupportIC = true
    private var isSupportMag = true
    //    private var isSupportNfc = true
    private var startMs: Long = 0
    //    private var ret: Int = 0
    var dialog: Dialog? = null

    private fun publishProgress(vararg values: String) {
        flow.runOnUiThread {
            flow.showProgressBar("Processing...", values[0])
//                , true) {
//                onClose {
//                    userCancel = true
//                    deviceClose()
//                }
//            }
        }
    }

    fun waitForCard(onEventChange: CardReaderEventListener) {
        watchJob = GlobalScope.launch(Dispatchers.Main) {
            flow.showProgressBar("Opening device", "Please wait...", true) {
                onClose {
                    userCancel = true
                    deviceClose()
                    onEventChange(CardReaderEvent.CANCELLED)
                }
            }
            withContext(Dispatchers.Default) {
                openDevice()
            }
            delay(500)
            updateCardWaitingProgress("Insert or swipe card")
            event = withContext(Dispatchers.Default) {
                detectCard()
            }

            if (event == CardReaderEvent.CANCELLED) return@launch

            if (event == CardReaderEvent.MAG_STRIPE) {
                data[0] = EmvService.MagStripeReadStripeData(1)
                data[1] = EmvService.MagStripeReadStripeData(2)
                data[2] = EmvService.MagStripeReadStripeData(3)
            }

            flow.runOnUiThread {
                dialog?.dismiss()
                onEventChange(event)
            }

            if (event == CardReaderEvent.CHIP) {
                startWatch(onEventChange)
            }
        }
    }

    suspend fun startWatch(onEventChange: CardReaderEventListener) {
        withContext(Dispatchers.Default) {
            while (true) {
                if (userCancel) break
                if (isSessionOver) break

                val ret = EmvService.IccCheckCard(300)
                appendDis("IccCheckCard:$ret")
                if (ret != 0) break
            }
        }

        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            flow.runOnUiThread {
                onEventChange(CardReaderEvent.REMOVED)
            }
        }
    }

    private fun cancelWatchJob() {
        try {
            watchJob?.cancel()
            watchJob = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun endWatch() {
        isSessionOver = true

        cancelWatchJob()

        try {
            readJob?.cancel()
            readJob = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun read(amountStr: String = "NGN0.00", onReadCard: CardDataListener) {
        try {
            readJob = GlobalScope.launch(Dispatchers.Main) {
                when (event) {
                    CardReaderEvent.MAG_STRIPE -> {
                        publishProgress("Please wait")

                        val cardData = withContext(Dispatchers.Default) {
                            val param = PinParam(flow)

                            param.KeyIndex = 0
                            param.WaitSec = 60
                            param.MaxPinLen = 4
                            param.MinPinLen = 4
                            param.IsShowCardNo = 0
                            param.Amount = amountStr

                            PinpadService.Open(flow)
                            WakeUpAndUnlock(flow).run()

                            val pinStatusCode = PinpadService.TP_PinpadGetPin(param)
                            val pinBlock = StringUtil.bytesToHexString(param.Pin_Block)
                            log("TP_PinpadGetPin: $pinStatusCode\nPinBlock: $pinBlock")
                            emvServiceListener.pinBlock = pinBlock

                            val emvResponse = when {
                                pinStatusCode == PinpadService.PIN_ERROR_CANCEL -> {
                                    log("get pin : user cancel")
                                    EmvService.ERR_USERCANCEL
                                }
                                pinStatusCode == PinpadService.PIN_OK && pinBlock == "00000000" -> {
                                    log("get pin : no pin")
                                    EmvService.ERR_NOPIN
                                }
                                pinStatusCode == PinpadService.PIN_OK -> {
                                    log("get pin success: " + StringUtil.bytesToHexString(param.Pin_Block))
                                    EmvService.EMV_TRUE
                                }
                                pinStatusCode == PinpadService.PIN_ERROR_TIMEOUT -> {
                                    log("get pin : timeout")
                                    EmvService.ERR_TIMEOUT
                                }
                                else -> {
                                    log("get pin error: $pinStatusCode")
                                    EmvService.EMV_FALSE
                                }
                            }

                            CardData(magStripData = data).apply {
                                ret = emvResponse
                                this.pinBlock = pinBlock
                            }
                        }

                        deviceClose()
                        flow.hideProgressBar()

                        flow.runOnUiThread {
                            onReadCard(cardData)
                        }
                    }

                    CardReaderEvent.CHIP -> {
                        var ret = 0
                        if (userCancel) {
                            flow.runOnUiThread {
                                onReadCard(null)
                            }

                            return@launch
                        }

                        publishProgress("IC card detected...")
                        withContext(Dispatchers.Default) {
                            ret = EmvService.IccCard_Poweron()
                            Log.w("readcard", "IccCard_Poweron: $ret")
                            ret = emvService.Emv_TransInit()
                            Log.w("readcard", "Emv_TransInit: $ret")

                            setEmvParams()
                            startMs = System.currentTimeMillis()

                            cancelWatchJob()

                            ret = emvService.Emv_StartApp(EmvService.EMV_FALSE)
                            Log.w("readcard", "Emv_StartApp: $ret")
                        }

                        deviceClose()
                        flow.hideProgressBar()

                        val cardData = withContext(Dispatchers.Default) {
                            CardData(if (ret == EmvService.EMV_TRUE) emvService else null)
                        }
                        cardData.ret = ret
                        cardData.pinBlock = emvServiceListener.pinBlock ?: ""

                        flow.runOnUiThread {
                            onReadCard(cardData)
                        }
                    }

                    else -> flow.runOnUiThread {
                        onReadCard(null)
                    }
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun log(s: String) {
        if (BuildConfig.DEBUG) Log.d("CardReader", s)
    }

    private fun setEmvParams() {
        val emvParam = EmvParam().apply {
            MerchName = "AppZone".toByteArray()
            MerchId = flow.parameters.parameters.cardAcceptorId.toByteArray()
            TermId = flow.config.terminalId.toByteArray()
            TerminalType = 0x22
            Capability = byteArrayOf(0xE0.toByte(), 0xF9.toByte(), 0xC8.toByte())
            ExCapability = byteArrayOf(0xE0.toByte(), 0x00, 0xF0.toByte(), 0xA0.toByte(), 0x01)
            CountryCode = byteArrayOf(5, 66)

            TransType = 0x00 //0x31
        }

        emvService.Emv_SetParam(emvParam)
    }

    private fun openDevice() {
        var ret: Int
        if (isSupportMag) {
            ret = EmvService.MagStripeOpenReader()
            appendDis("MagStripeOpenReader:$ret")
        }

        if (isSupportIC) {
            ret = EmvService.IccOpenReader()
            appendDis("IccOpenReader:$ret")
        }

//        if (isSupportNfc) {
//            ret = EmvService.NfcOpenReader(1000)
//            appendDis("NfcOpenReader:$ret")
//        }
    }

    private fun deviceClose() {
        var ret: Int
        if (isSupportMag) {
            ret = EmvService.MagStripeCloseReader()
            appendDis("MagStripeCloseReader:$ret")
        }

        if (isSupportIC) {
            if (event == CardReaderEvent.CHIP) {
                ret = EmvService.IccCard_Poweroff()
                appendDis("IccCard_Poweroff:$ret")
            }
            ret = EmvService.IccCloseReader()
            appendDis("IccCloseReader:$ret")
        }

//        if (isSupportNfc) {
//            ret = EmvService.NfcCloseReader()
//            appendDis("NfcCloseReader:$ret")
//        }
    }

    private fun checkForHybrid(cardData: Array<String?>): Boolean {
        return true
    }

    private fun appendDis(msg: String) {
        Log.d("MyEMV", msg)
    }

    private fun powerOnIcc(): Boolean {
        val ret = EmvService.IccCard_Poweron()
        EmvService.IccCard_Poweroff()

        return ret == EmvService.EMV_DEVICE_TRUE
    }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        flow.runOnUiThread {
            dialog = flow.showProgressBar(text, "Waiting...", isCancellable = true) {
                onClose {
                    userCancel = true
                    deviceClose()
                    if (!flow.isFinishing) flow.finish()
                }
            }
        }
    }

    private fun detectCard(): CardReaderEvent {
        var ret: Int
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            if (isSupportMag) {
                ret = EmvService.MagStripeCheckCard(1000)
                appendDis("MagStripeCheckCard:$ret")
                if (ret == 0) {
                    if (!chipFailure && checkForHybrid(data)) {
                        hybridDetected = true

                        updateCardWaitingProgress("Card is chip card. Please Insert Card")
                    } else return CardReaderEvent.MAG_STRIPE
                }
            }

            if (isSupportIC) {
                ret = EmvService.IccCheckCard(300)
                appendDis("IccCheckCard:$ret")

                if (ret == 0) {
                    val powerOn = powerOnIcc()

                    if (powerOn) return CardReaderEvent.CHIP

//                    hybridDetected = true

                    if (!powerOn && !hybridDetected) {
                        return CardReaderEvent.CHIP_FAILURE
                    }

                    chipFailure = !powerOn

                    updateCardWaitingProgress("ICC Failure. Please Swipe Card")
                }
            }
        }
    }
}
