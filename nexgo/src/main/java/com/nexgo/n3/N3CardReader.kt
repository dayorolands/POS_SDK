package com.nexgo.n3

import android.os.Environment
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.*
import com.creditclub.pos.extensions.hexBytes
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener
import com.nexgo.oaf.apiv3.emv.EmvEntryModeEnum
import com.nexgo.oaf.apiv3.emv.EmvProcessFlowEnum
import com.nexgo.oaf.apiv3.emv.EmvTransConfigurationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class N3CardReader(
    private val activity: CreditClubActivity,
    private val deviceEngine: DeviceEngine,
    private val sessionData: PosManager.SessionData,
    private val posManager: N3PosManager,
    private val defaultPosParameter:PosParameter,
    private val posConfig: PosConfig,
) : CardReader {
    private var cardReader = deviceEngine.cardReader
    private val emvHandler2 = deviceEngine.getEmvHandler2("app2")
    private val dialogProvider = activity.dialogProvider

    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false
    private var isSessionOver = false

    private var supportsChip = true
    private var supportsMagStripe = true

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        withContext(Dispatchers.IO) { openDevice() }
        delay(500)
        updateCardWaitingProgress("Insert or swipe card")
        cardReaderEvent = withContext(Dispatchers.IO) { detectCard() }

        dialogProvider.hideProgressBar()

        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData: CardData? = suspendCoroutine { continuation ->
            dialogProvider.showProgressBar("Processing", "IC card detected...") {
                onClose {
                    emvHandler2.emvProcessCancel()
                    continuation.resume(null)
                }
            }
            val emvListener = N3EmvListener(
                activity = activity,
                deviceEngine = deviceEngine,
                sessionData = sessionData,
                continuation = continuation,
                defaultPosParameter = defaultPosParameter,
            )
            emvHandler2.setTlv("9F33".hexBytes, "E040C8".hexBytes)
            emvHandler2.emvProcess(transData, emvListener)
        }
        safeRunIO {
            debugOnly {
                Runtime.getRuntime().exec(
                    "logcat -v time -f " + Environment.getExternalStorageDirectory()
                        .path + "/" + "emvlog_" + LocalDateTime.now().toString()
                )
            }
        }
        dialogProvider.hideProgressBar()
        return cardData
    }

    override fun endWatch() {
        isSessionOver = true
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        withContext(Dispatchers.IO) {
            while (true) {
                if (userCancel) break
                if (isSessionOver) break
                if (checkCard(CardSlotTypeEnum.ICC1, 1) != 0) break
            }
        }

        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
        }
    }

    private inline val transData
        get() = EmvTransConfigurationEntity().apply {
            transAmount = posManager
                .sessionData
                .amount
                .toString()
                .padStart(12, '0')

            emvTransType = when (posManager.sessionData.transactionType) {
                TransactionType.CashBack -> {
                    cashbackAmount = posManager
                        .sessionData
                        .cashBackAmount
                        .toString()
                        .padStart(12, '0')

                    0x09
                }
                TransactionType.Refund -> 0x20
                else -> 0x00.toByte()
            }

            countryCode = "0566" //CountryCode
            currencyCode = "0566" //CurrencyCode
            termId = posConfig.terminalId
            merId = defaultPosParameter.managementData.cardAcceptorId
            transDate =
                SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
            transTime =
                SimpleDateFormat("hhmmss", Locale.getDefault()).format(Date())
            traceNo = "00000000"
            emvProcessFlowEnum = EmvProcessFlowEnum.EMV_PROCESS_FLOW_STANDARD
            emvEntryModeEnum = if (cardReaderEvent == CardReaderEvent.CHIP) {
                EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACT
            } else {
                EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACTLESS
            }
//            isContactForceOnline = true
        }

    private fun openDevice() {
        if (supportsMagStripe) cardReader.open(CardSlotTypeEnum.SWIPE)
        if (supportsChip) cardReader.open(CardSlotTypeEnum.ICC1)
//        if (isSupportNfc) EmvService.NfcOpenReader(1000)
    }

    private fun deviceClose() {
        if (supportsMagStripe) cardReader.close(CardSlotTypeEnum.SWIPE)

        if (supportsChip) {
            if (cardReaderEvent == CardReaderEvent.CHIP) cardReader.close(CardSlotTypeEnum.ICC1)
        }

//        if (isSupportNfc) {
//            EmvService.NfcCloseReader()
//        }
    }

    private fun powerOnIcc(): Boolean {
        val ret = 1
        cardReader.open(CardSlotTypeEnum.ICC1)
        cardReader.stopSearch()

        return ret == 1
    }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        dialogProvider.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!activity.isFinishing) activity.finish()
            }
        }
    }

    private suspend fun checkCard(cardSlotType: CardSlotTypeEnum, timeout: Int = 60): Int =
        suspendCoroutine {
            cardReader.searchCard(hashSetOf(cardSlotType), timeout, object : OnCardInfoListener {
                override fun onCardInfo(retCode: Int, cardInfo: CardInfoEntity?) {
                    it.resume(retCode)
                }

                override fun onSwipeIncorrect() {
                    it.resume(-1)
                }

                override fun onMultipleCards() {
                    it.resume(-1)
                }
            })
        }

    private suspend fun detectCard(): CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            if (supportsMagStripe) {
                val ret = checkCard(CardSlotTypeEnum.SWIPE, 1)
                if (ret == 0) {
                    if (!chipFailure) {
                        hybridDetected = true

                        updateCardWaitingProgress("Card is chip card. Please Insert Card")
                    } else return CardReaderEvent.MAG_STRIPE
                }
            }

            if (supportsChip) {
                val ret = checkCard(CardSlotTypeEnum.ICC1, 1)

                if (ret == 0) {
                    val powerOn = true//powerOnIcc()

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