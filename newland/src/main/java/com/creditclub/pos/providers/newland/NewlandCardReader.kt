package com.creditclub.pos.providers.newland

import android.text.TextUtils
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.card.CardReaders
import com.cluster.pos.extensions.hexByte
import com.cluster.pos.extensions.hexBytes
import com.creditclub.pos.providers.newland.util.EmvL3Configuration
import com.newland.nsdk.core.api.common.ModuleType
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardInfo
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardType
import com.newland.nsdk.core.api.common.card.magcard.MagCardInfo
import com.newland.nsdk.core.api.common.cardreader.CardReaderListener
import com.newland.nsdk.core.api.common.cardreader.CardReaderParameters
import com.newland.nsdk.core.api.common.cardreader.CardType
import com.newland.nsdk.core.api.common.crypto.AlgorithmParameters
import com.newland.nsdk.core.api.common.crypto.KCVMode
import com.newland.nsdk.core.api.common.exception.NSDKException
import com.newland.nsdk.core.api.common.keymanager.CipherMode
import com.newland.nsdk.core.api.common.keymanager.KeyGenerateMethod
import com.newland.nsdk.core.api.common.keymanager.KeyType
import com.newland.nsdk.core.api.common.keymanager.KeyUsage
import com.newland.nsdk.core.api.common.keymanager.SymmetricKey
import com.newland.nsdk.core.api.internal.NSDKModuleManager
import com.newland.nsdk.core.api.internal.cardreader.CardReader
import com.newland.nsdk.core.api.internal.keymanager.KeyManager
import com.newland.sdk.emvl3.api.common.EmvL3Const
import com.newland.sdk.emvl3.api.common.util.BytesUtils
import com.newland.sdk.emvl3.api.common.util.LoggerUtils
import com.newland.sdk.emvl3.api.internal.EmvL3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewlandCardReader(
    private val ordaActivity: CreditClubActivity,
    private val emvL3: EmvL3,
    private val nsdkModuleManager: NSDKModuleManager,
    private val sessionData: PosManager.SessionData
) : CardReaders, KoinComponent{
    private val cardReader = nsdkModuleManager.getModule(ModuleType.CARD_READER) as CardReader
    private val mKeyManager = nsdkModuleManager.getModule(ModuleType.KEY_MANAGER) as KeyManager
    private val dialogProvider = ordaActivity.dialogProvider
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED
    private var cardInterface = 0
    private var userCancel = false
    private var isSessionOver = false

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        updateCardWaitingProgress("Insert or Swipe Card")
        cardReaderEvent = withContext(Dispatchers.Unconfined){
            detectCard()
        }
        dialogProvider.hideProgressBar()
        return cardReaderEvent
    }

    private fun deviceClose(){
        cardReader.cancelCardReader()
    }

    private suspend fun detectCard() : CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            val ret = checkCard()

            if (ret == CardReaderEvent.MAG_STRIPE) {
                if (!chipFailure) {
                    hybridDetected = true
                    updateCardWaitingProgress("Card is chip card. Please Insert Card")
                }
                else
                    return CardReaderEvent.MAG_STRIPE
            }
            else if (ret == CardReaderEvent.CHIP) {
                val powerOn = cardReader.isCardInserted

                if (powerOn){
                    return CardReaderEvent.CHIP
                }

                if (!powerOn && !hybridDetected) {
                    return CardReaderEvent.CHIP_FAILURE
                }


                chipFailure = !powerOn

                updateCardWaitingProgress("ICC Failure. Please Swipe Card")
            }
        }
    }

    private suspend fun checkCardMock() : CardReaderEvent =
        suspendCoroutine {
            cardInterface = EmvL3Const.CardInterface.CONTACT
            it.resume(CardReaderEvent.CHIP)
        }

    private suspend fun checkCard() : CardReaderEvent =
        suspendCoroutine {
            val cardTypes = arrayOf(CardType.MAG_CARD, CardType.CONTACT_CARD, CardType.CONTACTLESS_CARD)
            val parameter = CardReaderParameters()
            parameter.contactlessCardTypes = arrayOf(ContactlessCardType.TYPE_F, ContactlessCardType.TYPE_A, ContactlessCardType.TYPE_B)
            cardReader.openCardReader(cardTypes, 45, parameter, object : CardReaderListener {
                override fun onTimeout() {
                    it.resume(CardReaderEvent.Timeout)
                }

                override fun onCancel() {
                    it.resume(CardReaderEvent.CANCELLED)
                }

                override fun onError(p0: Int, p1: String?) {
                    it.resume(CardReaderEvent.CANCELLED)
                }

                override fun onFindMagCard(p0: MagCardInfo?) {
                    it.resume(CardReaderEvent.MAG_STRIPE)
                }

                override fun onFindContactCard() {
                    cardInterface = EmvL3Const.CardInterface.CONTACT
                    it.resume(CardReaderEvent.CHIP)
                }

                override fun onFindContactlessCard(
                    p0: ContactlessCardType?,
                    p1: ContactlessCardInfo?
                ) {
                    it.resume(CardReaderEvent.NFC)
                }

            })
        }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        dialogProvider.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!ordaActivity.isFinishing) ordaActivity.finish()
            }
        }
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData : CardData? = suspendCoroutine { continuation ->
            dialogProvider.showProgressBar("Processing", "IC card detected...") {
                onClose {
                    continuation.resume(null)
                }
            }
            loadKeys()
            val newlandEmvListener = NewlandEmvListener(emvL3, ordaActivity, sessionData, continuation)
            val concatenatedTransactionData = getTransactionData().hexBytes
            CoroutineScope(Dispatchers.IO).launch {
                emvL3.performTransaction(cardInterface, 60, concatenatedTransactionData, newlandEmvListener)
            }
        }

        return cardData
    }

    private fun loadKeys(){
        loadPlainMainKey("9797A76D40B33D37D004859213A28334", null, 1)
        loadWorkingKey(1, "624930E5B23F711F624930E5B23F711F", null, 1, 2)
    }

    @Throws(NSDKException::class)
    private fun loadPlainMainKey(
        mainKey: String,
        kcv: String?,
        keyIndex: Int
    ) {
        LoggerUtils.d("loadCipherInnerMainKey")
        val desKey = SymmetricKey()
        desKey.keyID = keyIndex.toByte()
        desKey.keyType = KeyType.DES
        desKey.keyUsage = KeyUsage.KEK
        desKey.keyLen = 16
        val bcdMainKey = BytesUtils.hexToBytes(mainKey)
        desKey.keyData = bcdMainKey
        if (TextUtils.isEmpty(kcv)) {
            desKey.kcvMode = KCVMode.NONE
        } else {
            desKey.kcvMode = KCVMode.ZERO
            desKey.kcv = BytesUtils.hexToBytes(kcv)
        }
        val algorithmParameters = AlgorithmParameters()
        algorithmParameters.cipherMode = CipherMode.ECB
        try {
            mKeyManager.generateKey(KeyGenerateMethod.CLEAR, algorithmParameters, null, desKey)
        } catch (e: NSDKException) {
            e.printStackTrace()
            LoggerUtils.d("loadPlainMainKey failed")
            return
        }
    }

    @Throws(NSDKException::class)
    fun loadWorkingKey(
        workKeyType: Int,
        keyValue: String?,
        kcv: String?,
        kekIndex: Int,
        workKeyIndex: Int
    ) {
        val sourceKey = SymmetricKey()
        val desKey = SymmetricKey()
        val algorithmParameters = AlgorithmParameters()
        when (workKeyType) {
            1 -> desKey.keyUsage = KeyUsage.PIN
            2 -> desKey.keyUsage = KeyUsage.MAC
            3 -> desKey.keyUsage = KeyUsage.DATA
        }
        //load work key
        val bcdKey = BytesUtils.hexToBytes(keyValue)
        sourceKey.keyID = kekIndex.toByte()
        sourceKey.keyType = KeyType.DES
        sourceKey.keyUsage = KeyUsage.KEK
        algorithmParameters.cipherMode = CipherMode.ECB
        desKey.keyID = workKeyIndex.toByte()
        desKey.keyType = KeyType.DES
        desKey.keyLen = bcdKey.size
        desKey.keyData = bcdKey
        if (TextUtils.isEmpty(kcv)) {
            desKey.kcvMode = KCVMode.NONE
        } else {
            desKey.kcvMode = KCVMode.ZERO
            desKey.kcv = BytesUtils.hexToBytes(kcv)
        }
        try {
            mKeyManager.generateKey(
                KeyGenerateMethod.CIPHER,
                algorithmParameters,
                sourceKey,
                desKey
            )
        } catch (e: NSDKException) {
            e.printStackTrace()
            LoggerUtils.d("loadWorkKey failed")
            return
        }
    }


    private fun getTransactionData() : String{
        val transactionAmount = sessionData.amount.toString().padStart(12, '0')
        val transAmountLength = transactionAmount.getTLVLength()
        val transactionType = "00"
        val transactionTypeLength = transactionType.getTLVLength()
        val transactionDate = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())
        val transactionDateLength = transactionDate.getTLVLength()
        val transactionTime = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        val transactionTimeLength = transactionTime.getTLVLength()
        val transactionCurrencyCode = "0566"
        val currencyCodeLength = transactionCurrencyCode.getTLVLength()

        return "9F02$transAmountLength$transactionAmount" +
                "9C$transactionTypeLength$transactionType" +
                "9A$transactionDateLength$transactionDate" +
                "9F21$transactionTimeLength$transactionTime" +
                "5F2A$currencyCodeLength$transactionCurrencyCode" +
                "1F812903C080001F813903FF0000"
    }

    private fun String.getTLVLength() = String.format("%02d", this.length.div(2))

    override fun endWatch() {
        cardReader.cancelCardReader()
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        withContext(Dispatchers.Default) {
            while (true) {
                if (userCancel) break
                if (isSessionOver) break
                if (!cardReader.isCardPresent) break
            }
        }

        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
        }
    }
}