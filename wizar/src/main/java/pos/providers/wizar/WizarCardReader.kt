package pos.providers.wizar

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.cloudpos.*
import com.cloudpos.jniinterface.EMVJNIInterface
import com.cloudpos.jniinterface.IFuntionListener
import com.cloudpos.pinpad.KeyInfo
import com.cloudpos.pinpad.PINPadDevice
import com.cloudpos.pinpad.PINPadOperationResult
import com.cloudpos.pinpad.extend.PINPadExtendDevice
import com.cloudpos.smartcardreader.SmartCardReaderDevice
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debugOnly
import com.cluster.core.util.format
import com.cluster.core.util.safeRun
import com.cluster.pos.DukptConfig
import com.cluster.pos.PosConfig
import com.cluster.pos.PosManager
import com.cluster.pos.PosParameter
import com.cluster.pos.card.*
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.cluster.pos.utils.asDesEdeKey
import com.cluster.pos.utils.decrypt
import com.cluster.pos.utils.encrypt
import com.wizarpos.emvsample.constant.EMVConstant
import com.wizarpos.emvsample.constant.EMVConstant.*
import com.wizarpos.emvsample.db.TransDetailInfo
import com.wizarpos.util.AppUtil
import com.wizarpos.util.ByteUtil
import com.wizarpos.util.StringUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.and

fun getSmartCardDevice(context: Context): SmartCardReaderDevice {
    return POSTerminal.getInstance(context)
        .getDevice("cloudpos.device.smartcardreader") as SmartCardReaderDevice
}

private const val TAG = "Wizar"
private const val PREF_CURRENT_MASTER_KEY_FIELD_NAME = "currentMasterKey"

class WizarCardReader(
    private val activity: CreditClubActivity,
    private val sessionData: PosManager.SessionData,
    private val defaultPosParameter: PosParameter,
    private val posConfig: PosConfig,
    private val terminalConfig: WizarTerminalConfig,
) : CardReader, IFuntionListener, EMVConstant {
    private val dialogProvider = activity.dialogProvider
    private val prefs =
        activity.getEncryptedSharedPreferences("pos.providers.wizar.cardreader")

    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false
    private var isSessionOver = false

    private var cardType = CARD_CONTACT
    private val cardData = WizarCardData()
    private var needCheckOfflinePin = false
    private var pinpadType = PINPAD_CUSTOM_UI // PINPAD_SYSTEM_UI
    private var promptOfflineDataAuthSucc = false
    private var ioScope = CoroutineScope(Dispatchers.IO)

    private val pinPad = POSTerminal.getInstance(activity)
        .getDevice("cloudpos.device.pinpad") as PINPadExtendDevice

    private var cardDataContinuation: Continuation<CardData?>? = null
    private var cardReaderEventContinuation: Continuation<CardReaderEvent>? = null

    override suspend fun waitForCard(): CardReaderEvent {
        EMVJNIInterface.registerFunctionListener(this)
        EMVJNIInterface.emv_kernel_initialize()
        EMVJNIInterface.emv_set_kernel_attr(byteArrayOf(0x04, 0x08), 2)
        EMVJNIInterface.emv_terminal_param_set_drl(byteArrayOf(0x00), 1)
        loadCAPK(defaultPosParameter)
        loadAID(defaultPosParameter)
        setEMVTermInfo(posConfig, defaultPosParameter)
        EMVJNIInterface.emv_set_force_online(1)
        debugOnly {
            Log.i("test", "kernel id:" + EMVJNIInterface.emv_get_kernel_id())
            Log.i("test", "process type:" + EMVJNIInterface.emv_get_process_type())
        }
        dialogProvider.showProgressBar(
            title = "Please insert card",
            message = "Waiting...",
            isCancellable = true,
        ) {
            onClose {
                userCancel = true
                if (!activity.isFinishing) activity.finish()
            }
        }
        cardReaderEvent = suspendCoroutine { continuation ->
            EMVJNIInterface.registerFunctionListener(CardInsertListener(continuation))
            EMVJNIInterface.open_reader(1)
        }
        EMVJNIInterface.registerFunctionListener(this)

        dialogProvider.hideProgressBar()

        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        endWatch()
        val cardData: CardData? = suspendCoroutine { continuation ->
            activity.runOnUiThread {
                dialogProvider.showProgressBar("Processing", "IC card detected...") {
                    onClose {
                        EMVJNIInterface.emv_stop_process()
                        continuation.resume(null)
                    }
                }
            }
            cardDataContinuation = continuation
            EMVJNIInterface.emv_anti_shake_finish(1)
            emvStart()
        }
        dialogProvider.hideProgressBar()
        return cardData
    }

    override fun endWatch() {
        isSessionOver = true
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        val cardEvent = suspendCoroutine<CardReaderEvent> { continuation ->
            cardReaderEventContinuation = continuation
        }

        if (!userCancel && !isSessionOver && cardEvent == CardReaderEvent.REMOVED) {
            userCancel = true
            onEventChange(CardReaderEvent.REMOVED)
        }
    }

    private fun emvStart() = thread {
        EMVJNIInterface.emv_trans_initialize()
        EMVJNIInterface.emv_set_kernel_type(CONTACT_EMV_KERNAL)

        // Set transaction amount
        val strAmt = sessionData.amount.toString()
        val amt = ByteArray(strAmt.length + 1)
        System.arraycopy(strAmt.toByteArray(), 0, amt, 0, strAmt.length)
        EMVJNIInterface.emv_set_trans_amount(amt)

        val instant = Instant.now()
        EMVJNIInterface.emv_set_tag_data(0x9A, instant.format("uuMMdd").hexBytes, 3)
        EMVJNIInterface.emv_set_tag_data(0x9F21,
            StringUtil.hexString2bytes(instant.format("HHmmss")),
            3)
        EMVJNIInterface.emv_set_tag_data(
            0x9F41,
            StringUtil.hexString2bytes(
                StringUtil.fillZero(terminalConfig.transactionSequenceNumber.toString(), 8)
            ),
            4
        )

        EMVJNIInterface.emv_set_trans_type(EMV_TRANS_GOODS_SERVICE)

        pinPad.open()
        pinPad.setGUIConfiguration("sound", "true")
        pinPad.close()

        EMVJNIInterface.emv_process_next()
    }

    private fun getTranType(): Byte {
        return if (sessionData.transactionType == TransactionType.Balance) {
            QUERY_CARD_RECORD
        } else {
            TRAN_GOODS
        }
    }

    override fun emvProcessCallback(data: ByteArray) {
        ioScope.launch {
            emvProcessCompleted(emvStatus = data[0], emvRetCode = data[1])
        }
    }

    private val trans = TransDetailInfo().apply { init() }

    override fun cardEventOccured(eventType: Int) {
        when (eventType) {
            SMART_CARD_EVENT_INSERT_CARD -> {
                val cardEvent = when (EMVJNIInterface.get_card_type()) {
                    CARD_CONTACT -> CardReaderEvent.CHIP
                    CARD_CONTACTLESS -> CardReaderEvent.MAG_STRIPE
                    else -> CardReaderEvent.HYBRID_FAILURE
                }
                handleCardEvent(cardEvent)
            }
            SMART_CARD_EVENT_POWERON_ERROR -> {
                handleCardEvent(CardReaderEvent.CHIP_FAILURE)
            }
            SMART_CARD_EVENT_REMOVE_CARD -> {
                cardType = -1
                isSessionOver = true
                handleCardEvent(CardReaderEvent.REMOVED)
            }
            SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD -> {
                handleCardEvent(CardReaderEvent.HYBRID)
            }
            SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE -> {
                handleCardEvent(CardReaderEvent.HYBRID_FAILURE)
            }
        }
    }

    private fun handleCardEvent(cardEvent: CardReaderEvent) {
        val continuation = cardReaderEventContinuation
        cardReaderEventContinuation = null
        continuation?.resume(cardEvent)
    }

    private fun emvProcessCompleted(emvStatus: Byte, emvRetCode: Byte) {
        val tagData: ByteArray
        var tagDataLength: Int
        when (emvStatus) {
            STATUS_CONTINUE -> when (emvRetCode) {
                EMV_CANDIDATE_LIST -> {
                    val aidList = ByteArray(300)
                    val aidListLength =
                        EMVJNIInterface.emv_get_candidate_list(aidList, aidList.size)

                    EMVJNIInterface.emv_set_candidate_list_result(0)
                }
                EMV_APP_SELECTED -> {
                    if (getTranType() == QUERY_CARD_RECORD || trans.transAmount > 0) {
                        emvNext()
                    } else {
                        val strAmt = sessionData.amount.toString()
                        val amt = ByteArray(strAmt.length + 1)
                        System.arraycopy(strAmt.toByteArray(), 0, amt, 0, strAmt.length)
                        EMVJNIInterface.emv_set_trans_amount(amt)
                        EMVJNIInterface.emv_set_other_amount(byteArrayOf('0'.code.toByte(), 0x00))
                        emvNext()
                    }
                }
                EMV_READ_APP_DATA -> {
                    //confirmCard();
                    readCardAppData()
                    emvNext()
                }
                EMV_DATA_AUTH -> {
                    val tsi = ByteArray(2)
                    val tvr = ByteArray(5)
                    EMVJNIInterface.emv_get_tag_data(0x9B, tsi, 2) // TSI
                    EMVJNIInterface.emv_get_tag_data(0x95, tvr, 5) // TVR
                    if ((tsi[0] and 0x80.toByte() == 0x80.toByte()) && (tvr[0] and 0x40.toByte() == 0x00.toByte()) && ((tvr.get(
                            0) and 0x08.toByte()) == 0x00.toByte()) && ((tvr.get(0) and 0x04.toByte()) == 0x00.toByte())
                    ) {
                        promptOfflineDataAuthSucc = true
                    }
                    emvNext()
                }
                EMV_OFFLINE_PIN -> {
                    if (pinpadType == PINPAD_CUSTOM_UI) {
                        inputPin(online = false)
                    } else {
                        needCheckOfflinePin = true
                        emvNext()
                    }
                }
                EMV_ONLINE_ENC_PIN -> {
                    if (pinpadType == PINPAD_NONE) {
                        EMVJNIInterface.emv_set_online_pin_entered(1)
                        emvNext()
                    } else {
                        inputPin(online = true)
                    }
                }
                EMV_PROCESS_ONLINE -> {
                    readCardAppData()
                    getEMVCardInfo()

                    EMVJNIInterface.emv_set_online_result(
                        ONLINE_SUCCESS.toInt(),
                        byteArrayOf(0, 0),
                        byteArrayOf(' '.code.toByte()),
                        0
                    )
                    emvNext()
                }
                else -> {
                    emvNext()
                }
            }
            STATUS_COMPLETION -> {
                terminalConfig.transactionSequenceNumber++
                trans.needSignature = EMVJNIInterface.emv_is_need_signature()
                tagData = ByteArray(50)
                if (EMVJNIInterface.emv_is_tag_present(0x95) >= 0) {
                    tagDataLength = EMVJNIInterface.emv_get_tag_data(0x95, tagData, tagData.size)
                    terminalConfig.lastTvr = StringUtil.toHexString(tagData,
                        0,
                        tagDataLength,
                        false)
                }
                if (EMVJNIInterface.emv_is_tag_present(0x9B) >= 0) {
                    tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9B, tagData, tagData.size)
                    terminalConfig.lastTsi = StringUtil.toHexString(tagData,
                        0,
                        tagDataLength,
                        false)
                }
                getEMVCardInfo()
                cardData.apply {
                    pan = trans.pan
                    track2 = trans.track2Data
                    src = getSrcFromTrack2(trans.track2Data)
                    status = CardTransactionStatus.Success
                    transactionAmount = getTagValue(0x9F02)
                    exp = getTagValue(0x5F24)
                    holder = getTagValue(0x5F20, true)
                    cardSequenceNumber = getTagValue(0x5f34)
                    aid = getTagValue(0x9F06)

                    cardMethod = CardReaderEvent.CHIP
                }
                val tvr = trans.tvr.hexBytes
                if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                    cardData.status = CardTransactionStatus.OfflinePinVerifyError
                }

                val continuation = cardDataContinuation
                cardDataContinuation = null
                continuation?.resume(cardData)
            }
            else -> when (emvRetCode) {
                ERROR_OTHER_CARD -> {
                    failWithStatus()
                }
                ERROR_EXPIRED_CARD -> {
                    failWithStatus(CardTransactionStatus.CardExpired)
                }
                ERROR_CARD_BLOCKED -> {
                    failWithStatus(CardTransactionStatus.CardRestricted)
                }
                ERROR_APP_BLOCKED -> {
                    failWithStatus(CardTransactionStatus.CardRestricted)
                }
                ERROR_SERVICE_NOT_ALLOWED -> {
                    failWithStatus()
                }
                ERROR_PINENTERY_TIMEOUT -> {
                    failWithStatus(CardTransactionStatus.Timeout)
                }
                ERROR_CONTACT_DURING_CONTACTLESS -> {
                    EMVJNIInterface.close_reader(2)

                    trans.emvCardError = false
                    trans.cardEntryMode = INSERT_ENTRY
                    cardType = CARD_CONTACT
                    if (EMVJNIInterface.open_reader_ex(1, 1) < 0) {
                        failWithStatus()
                    }
                }
                ERROR_PROCESS_CMD -> {
                    failWithStatus()
                }
                else -> {
                    failWithStatus()
                }
            }
        }
        return
    }

    private fun emvNext() = thread {
        EMVJNIInterface.emv_process_next()
    }

    private fun getSrcFromTrack2(track2: String): String {
        var markerIndex = track2.indexOf("D")
        if (markerIndex < 0) {
            markerIndex = track2.indexOf("=")
        }
        return track2.substring(markerIndex + 5, markerIndex + 8)
    }

    private fun inputPin(online: Boolean) {
        if (!online) {
            EMVJNIInterface.emv_process_next()
            return
        }
        val posParameter: PosParameter =
            sessionData.getPosParameter?.invoke(cardData.pan, sessionData.amount / 100.0)
                ?: defaultPosParameter
        val dukptConfig: DukptConfig? =
            sessionData.getDukptConfig?.invoke(cardData.pan, sessionData.amount / 100.0)

        pinPad.open()

        val masterKeyString = posParameter.masterKey
        val masterKey = masterKeyString.hexBytes
        val localMasterKey = ByteArray(16) { 0x38.toByte() }
        val newPinKey = masterKey.asDesEdeKey.encrypt(posParameter.pinKey.hexBytes)

        if (dukptConfig == null) {
            safeRun {
                pinPad.updateMasterKey(0, localMasterKey, masterKey)
                prefs.edit {
                    putString(PREF_CURRENT_MASTER_KEY_FIELD_NAME, masterKeyString)
                }
            }
            safeRun { pinPad.updateUserKey(0, 0, newPinKey) }
        }

        pinPad.setPINLength(4, 4)

        val pinListener = OperationListener { operationResult ->
            when (operationResult.resultCode) {
                OperationResult.SUCCESS -> {
                    // PIN block encrypted by the sdk
                    val localPinBlock = (operationResult as PINPadOperationResult).encryptedPINBlock
                    if (operationResult.resultCode == OperationResult.SUCCESS && localPinBlock != null && localPinBlock.isNotEmpty()) {
                        if (dukptConfig == null) {
                            val localPinKey = masterKey.asDesEdeKey.decrypt(newPinKey)
                            val pinPanXor = localPinKey.asDesEdeKey.decrypt(localPinBlock)
                            val remoteSecretKey = posParameter.pinKey.hexBytes.asDesEdeKey

                            // PIN block encrypted by remote pin key
                            val remotePinBlock = remoteSecretKey.encrypt(pinPanXor)
                            cardData.pinBlock = remotePinBlock.hexString
                        }
                    }
                    EMVJNIInterface.emv_set_online_pin_entered(1)
                    emvNext()
                }
                OperationResult.CANCEL -> {
                    failWithStatus(CardTransactionStatus.UserCancel)
                }
                OperationResult.ERR_TIMEOUT -> {
                    failWithStatus(CardTransactionStatus.Timeout)
                }
                else -> {
                    failWithStatus()
                }
            }
            pinPad.close()
        }
        val keyInfo = if (dukptConfig == null) {
            KeyInfo(
                PINPadDevice.KEY_TYPE_MK_SK,
                0,
                0,
                AlgorithmConstants.ALG_3DES
            )
        } else {
            KeyInfo(
                PINPadDevice.KEY_TYPE_TDUKPT,
                0,
                0,
                AlgorithmConstants.ALG_3DES
            )
        }
        pinPad.listenForPinBlock(
            keyInfo,
            cardData.pan,
            false,
            pinListener,
            60 * TimeConstants.SECOND
        )
    }

    private fun getEMVCardInfo() {
        val tagData = ByteArray(100)
        var tagDataLength: Int
        val iccData = ByteArray(1200)
        val tagList = defaultTagList
        val offset: Int = EMVJNIInterface.emv_get_tag_list_data(
            tagList,
            tagList.size,
            iccData,
            iccData.size
        )

        trans.setICCData(iccData, 0, offset)
        cardData.mIccString = ByteUtil.arrayToHexStr(iccData, offset)

        // Application Label 50
        if (EMVJNIInterface.emv_is_tag_present(0x50) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x50, tagData, tagData.size)
            val appLabel = ByteArray(tagDataLength)
            System.arraycopy(tagData, 0, appLabel, 0, appLabel.size)
            trans.appLabel = StringUtil.toString(appLabel)
        }

        // AIP
        if (EMVJNIInterface.emv_is_tag_present(0x82) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x82, tagData, tagData.size)
            trans.aip = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }

        // TVR
        if (EMVJNIInterface.emv_is_tag_present(0x95) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x95, tagData, tagData.size)
            trans.tvr = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }

        // TSI
        if (EMVJNIInterface.emv_is_tag_present(0x9B) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9B, tagData, tagData.size)
            trans.tsi = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }


        // Application Identifier terminal
        if (EMVJNIInterface.emv_is_tag_present(0x9F06) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F06, tagData, tagData.size)
            trans.aid = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }

        // IAD
        if (EMVJNIInterface.emv_is_tag_present(0x9F10) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F10, tagData, tagData.size)
            trans.iad = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }

        // ApplicationPreferredName  9F12
        if (EMVJNIInterface.emv_is_tag_present(0x9F12) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F12, tagData, tagData.size)
            val appName = ByteArray(tagDataLength)
            System.arraycopy(tagData, 0, appName, 0, appName.size)
            trans.appName = StringUtil.toString(appName)
        }
        if (EMVJNIInterface.emv_is_tag_present(0x9F26) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F26, tagData, tagData.size)
            trans.ac = StringUtil.toHexString(tagData, 0, tagDataLength, false)
        }
        if (EMVJNIInterface.emv_is_tag_present(0x9F37) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F37, tagData, tagData.size)
            trans.unpredictableNumber = StringUtil.toHexString(
                tagData,
                0,
                tagDataLength,
                false
            )
        }
        if (EMVJNIInterface.emv_is_tag_present(0x9F79) >= 0
            && trans.ecBalance < 0
        ) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x9F79, tagData, tagData.size)
            val amt = ByteArray(tagDataLength)
            System.arraycopy(tagData, 0, amt, 0, amt.size)
            trans.ecBalance = ByteUtil.bcdToInt(amt)
        }
        if (needCheckOfflinePin) {
            var wrongOfflinePinTimes = 0
            val offlinePinVerified = EMVJNIInterface.emv_offlinepin_verified()
            val offlinepinTimes = EMVJNIInterface.emv_get_offlinepin_times()
            if (offlinepinTimes > 0) {
                wrongOfflinePinTimes = offlinepinTimes - if (offlinePinVerified == 1) 1 else 0
            }
            if (debug) Log.d(TAG,
                "offlinePinVerified = $offlinePinVerified")
            if (debug) Log.d(TAG,
                "Wrong offline Pin Times = $wrongOfflinePinTimes")
            if (offlinePinVerified == 1) {
                trans.pinEntryMode = CAN_PIN // Offline PIN Verified
            } else if (offlinePinVerified == -1) {
                if (debug) Log.d(TAG,
                    "Wrong offline Pin")
            }
        }
    }

    private fun readCardAppData() {
        var tagData: ByteArray
        var tagDataLength: Int
        if (EMVJNIInterface.emv_is_tag_present(0x9F79) >= 0) {
            tagData = ByteArray(6)
            EMVJNIInterface.emv_get_tag_data(0x9F79, tagData, 6)
            trans.ecBalance = ByteUtil.bcdToInt(tagData)
        }
        tagData = ByteArray(100)
        if (EMVJNIInterface.emv_is_tag_present(0x5A) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x5A, tagData, tagData.size)
            trans.pan = StringUtil.toString(AppUtil.removeTailF(ByteUtil.bcdToAscii(
                tagData,
                0,
                tagDataLength)))
            cardData.pan = trans.pan
        }
        // Track2
        if (EMVJNIInterface.emv_is_tag_present(0x57) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x57, tagData, tagData.size)
            trans.track2Data = StringUtil.toString(AppUtil.removeTailF(
                ByteUtil.bcdToAscii(tagData, 0, tagDataLength)))
        }
        // CSN
        if (EMVJNIInterface.emv_is_tag_present(0x5F34) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x5F34, tagData, tagData.size)
            trans.csn = tagData[0]
        }
        // Expiry
        if (EMVJNIInterface.emv_is_tag_present(0x5F24) >= 0) {
            tagDataLength = EMVJNIInterface.emv_get_tag_data(0x5F24, tagData, tagData.size)
            trans.expiry = StringUtil.toHexString(tagData, 0, 3, false).substring(0, 4)
        }
    }

    private fun failWithStatus(status: CardTransactionStatus = CardTransactionStatus.Error) {
        cardData.status = status
        val continuation = cardDataContinuation
        cardDataContinuation = null
        continuation?.resume(cardData)
    }
}

private val defaultTagList = intArrayOf(
    0x5F2A,
    0x82,
    0x84,
    0x95,
    0x9A,
    0x9C,
    0x9F02,
    0x9F03,
    0x9F09,
    0x9F10,
    0x9F1A,
    0x9F26,
    0x9F27,
    0x9F33,
    0x9F34,
    0x9F35,
    0x9F36,
    0x9F37,
    0x9F41
)
