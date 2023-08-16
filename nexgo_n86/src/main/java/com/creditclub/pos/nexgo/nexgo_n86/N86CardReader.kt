package com.creditclub.pos.nexgo.nexgo_n86

import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debugOnly
import com.cluster.core.util.safeRunIO
import com.cluster.pos.PosConfig
import com.cluster.pos.PosManager
import com.cluster.pos.PosParameter
import com.cluster.pos.card.*
import com.cluster.pos.extensions.hexBytes
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener
import com.nexgo.oaf.apiv3.emv.EmvEntryModeEnum
import com.nexgo.oaf.apiv3.emv.EmvHandler2
import com.nexgo.oaf.apiv3.emv.EmvProcessFlowEnum
import com.nexgo.oaf.apiv3.emv.EmvTransConfigurationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class N86CardReader(
    private val activity: CreditClubActivity,
    private val deviceEngine: DeviceEngine,
    private val sessionData: PosManager.SessionData,
    private val posManager: N86PosManager,
    private val defaultPosParameter: PosParameter,
    private val posConfig: PosConfig,
) : CardReaders {
    private var cardReader = deviceEngine.cardReader
    private val emvHandler2 : EmvHandler2 = deviceEngine.getEmvHandler2("app2")
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
            val emvListener = N86EmvListener(
                activity = activity,
                deviceEngine = deviceEngine,
                sessionData = sessionData,
                continuation = continuation,
                defaultPosParameter = defaultPosParameter,
            )
            Log.d("CardInfoEntity", "Trans Amount : ${posManager.sessionData.amount.toString().padStart(12, '0')}")
            Log.d("CardInfoEntity", "Trans type : ${transData.emvTransType}")
            Log.d("CardInfoEntity", "Trans date : ${transData.transDate} and trans time ${transData.transTime}")
            emvHandler2.setTlv("9F33".hexBytes, "E040C8".hexBytes)
            emvHandler2.emvProcess(transData, emvListener)
        }
        dialogProvider.hideProgressBar()
        return cardData
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

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        dialogProvider.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!activity.isFinishing) activity.finish()
            }
        }
    }

    private fun deviceClose() {
        if (supportsMagStripe) cardReader.close(CardSlotTypeEnum.SWIPE)

        if (supportsChip) {
            if (cardReaderEvent == CardReaderEvent.CHIP) cardReader.close(CardSlotTypeEnum.ICC1)
        }
    }

    private fun openDevice() {
        if (supportsMagStripe) cardReader.open(CardSlotTypeEnum.SWIPE)
        if (supportsChip) cardReader.open(CardSlotTypeEnum.ICC1)
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