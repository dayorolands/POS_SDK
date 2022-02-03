package pos.providers.telpo

import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debugOnly
import com.cluster.core.util.hideProgressBar
import com.cluster.core.util.showProgressBar
import com.cluster.pos.PosConfig
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReader
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.telpo.emv.EmvParam
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinParam
import com.telpo.pinpad.PinpadService
import com.telpo.emv.util.hexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class TelpoCardReader(
    private val flow: CreditClubActivity,
    private val posManager: TelpoPosManager
) : CardReader, KoinComponent {
    private val emvService: EmvService = posManager.emvService
    private var data = arrayOfNulls<String>(3)
    private var event: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false
    private var isSessionOver = false

    private var supportsChip = true
    private var supportsMagStripe = true

    private fun publishProgress(vararg values: String) {
        flow.showProgressBar("Processing...", values[0])
    }

    override suspend fun waitForCard(): CardReaderEvent {
        flow.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        withContext(Dispatchers.Default) { openDevice() }
        delay(500)
        updateCardWaitingProgress("Insert or swipe card")
        event = withContext(Dispatchers.Default) { detectCard() }

        if (event == CardReaderEvent.MAG_STRIPE) {
            data[0] = EmvService.MagStripeReadStripeData(1)
            data[1] = EmvService.MagStripeReadStripeData(2)
            data[2] = EmvService.MagStripeReadStripeData(3)
        }

        flow.hideProgressBar()

        return event
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        withContext(Dispatchers.Default) {
            while (true) {
                if (userCancel) break
                if (isSessionOver) break
                if (EmvService.IccCheckCard(300) != 0) break
            }
        }

        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
        }
    }

    override fun endWatch() {
        isSessionOver = true
    }

    override suspend fun read(amountStr: String): CardData? {
        val emvListener = TelpoEmvListener(flow, emvService, posManager.sessionData)
        emvService.setListener(emvListener)
        try {
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
                        val pinBlock = param.Pin_Block.hexString
                        log("TP_PinpadGetPin: $pinStatusCode\nPinBlock: $pinBlock")
                        emvListener.pinBlock = pinBlock

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
                                log("get pin success: ")
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

                        TelpoEmvCardData(magStripData = data).apply {
                            ret = emvResponse
                            this.pinBlock = pinBlock
                        }
                    }

                    deviceClose()
                    flow.hideProgressBar()

                    return cardData
                }

                CardReaderEvent.CHIP -> {
                    if (userCancel) {
                        return null
                    }

                    publishProgress("IC card detected...")
                    val ret = withContext(Dispatchers.Default) {
                        EmvService.IccCard_Poweron()
                        emvService.Emv_TransInit()
                        setEmvParams()

                        emvService.Emv_SetOfflinePiDisplayPan(0)
//                        emvService.Emv_SetOfflinePinCBenable(1)
                        emvService.Emv_StartApp(EmvService.EMV_FALSE)
                    }

                    deviceClose()
                    flow.hideProgressBar()

                    val statusRet = emvListener.status?.code ?: ret
                    val cardData = emvListener.cardData ?: withContext(Dispatchers.Default) {
                        TelpoEmvCardData(if (statusRet == EmvService.EMV_TRUE) emvService else null)
                    }
                    cardData.ret = statusRet
                    cardData.pinBlock = emvListener.pinBlock ?: ""

                    return cardData
                }

                else -> return null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return null
    }

    private fun log(s: String) {
        debugOnly { Log.d("CardReader", s) }
    }

    private fun setEmvParams() {
        val managementData = get<PosParameter>().managementData
        val emvParam = EmvParam().apply {
            MerchName = "AppZone".toByteArray()
            MerchId = managementData.cardAcceptorId.toByteArray()
            MerchCateCode = managementData.merchantCategoryCode.toByteArray()
            TermId = get<PosConfig>().terminalId.toByteArray()
            TerminalType = 0x22
            Capability = byteArrayOf(0xE0.toByte(), 0x40.toByte(), 0xC8.toByte())
            ExCapability = byteArrayOf(0xE0.toByte(), 0x00, 0xF0.toByte(), 0xA0.toByte(), 0x01)
            CountryCode = byteArrayOf(0x5, 0x66)

            TransType = 0x00
        }

        emvService.Emv_SetParam(emvParam)
    }

    private fun openDevice() {
        if (supportsMagStripe) EmvService.MagStripeOpenReader()
        if (supportsChip) EmvService.IccOpenReader()
//        if (isSupportNfc) EmvService.NfcOpenReader(1000)
    }

    private fun deviceClose() {
        if (supportsMagStripe) EmvService.MagStripeCloseReader()

        if (supportsChip) {
            if (event == CardReaderEvent.CHIP) EmvService.IccCard_Poweroff()
            EmvService.IccCloseReader()
        }

//        if (isSupportNfc) {
//            EmvService.NfcCloseReader()
//        }
    }

    private fun powerOnIcc(): Boolean {
        val ret = EmvService.IccCard_Poweron()
        EmvService.IccCard_Poweroff()

        return ret == EmvService.EMV_DEVICE_TRUE
    }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        flow.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!flow.isFinishing) flow.finish()
            }
        }
    }

    private fun detectCard(): CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            if (supportsMagStripe) {
                val ret = EmvService.MagStripeCheckCard(1000)
                if (ret == 0) {
                    if (!chipFailure) {
                        hybridDetected = true

                        updateCardWaitingProgress("Card is chip card. Please Insert Card")
                    } else return CardReaderEvent.MAG_STRIPE
                }
            }

            if (supportsChip) {
                val ret = EmvService.IccCheckCard(300)

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
