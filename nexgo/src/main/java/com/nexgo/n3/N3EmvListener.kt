package com.nexgo.n3

import androidx.core.content.edit
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardTransactionStatus
import com.creditclub.pos.extensions.hexBytes
import com.creditclub.pos.extensions.hexString
import com.creditclub.pos.utils.TripleDesCipher
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.pinpad.*
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.emv.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class N3EmvListener(
    private val activity: CreditClubActivity,
    private val deviceEngine: DeviceEngine,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>
) : OnEmvProcessListener2, KoinComponent {
    private val emvHandler2 = deviceEngine.getEmvHandler2("app2")
    private val dialogProvider = activity.dialogProvider
    private val cardData = N3CardData()
    private var filed55: String? = null
    private val prefs = activity.getSharedPreferences("N3PosManager", 0)
    private val posParameter: PosParameter by inject()

    override fun onSelApp(
        appNameList: List<String>?,
        appInfoList: List<CandidateAppInfoEntity>?,
        isFirstSelect: Boolean
    ) = activity.runOnUiThread {
        val options = appNameList?.map { DialogOptionItem(it) }
        options ?: return@runOnUiThread
        dialogProvider.showOptions("Select app", options) {
            onSubmit { position -> emvHandler2.onSetSelAppResponse(position + 1) }
        }
    }

    override fun onTransInitBeforeGPO() {
        emvHandler2.onSetTransInitBeforeGPOResponse(true)
    }

    override fun onConfirmCardNo(cardInfo: CardInfoEntity) = activity.runOnUiThread {
        val cardNo = cardInfo.cardNo
        dialogProvider.confirm("Please Confirm Card Number", cardNo) {
            onSubmit { emvHandler2.onSetConfirmCardNoResponse(true) }
            onClose { emvHandler2.onSetConfirmCardNoResponse(false) }
        }
    }

    override fun onCardHolderInputPin(isOnlinePin: Boolean, leftTimes: Int) {
        val pinPad: PinPad = deviceEngine.pinPad
        val cardNo = emvHandler2.emvCardDataInfo.cardNo
        val amount = sessionData.amount / 100.0
        val dukptConfig = sessionData.getDukptConfig?.invoke(cardNo, amount)
        if (dukptConfig != null) {
            val paddedIpek = dukptConfig.ipek.padStart(20, '0')
            val paddedKsn = dukptConfig.ksn.padStart(20, '0')

            val ipekBytes = paddedIpek.hexBytes
            val ksnBytes = paddedKsn.hexBytes

            val xorValue = ipekBytes xor ksnBytes
            val kcv = xorValue.hexString.takeLast(6)
            if (prefs.getString("kcv", null) != kcv) {
                pinPad.dukptKeyInject(
                    0,
                    DukptKeyTypeEnum.IPEK,
                    ipekBytes,
                    ipekBytes.size, ksnBytes
                )
                prefs.edit { putString("kcv", kcv) }
            }
            pinPad.dukptKsnIncrease(0)
        } else {
            val masterKey = posParameter.masterKey.hexBytes
            pinPad.writeMKey(0, masterKey, masterKey.size)
            val pinKey = posParameter.pinKey.hexBytes
            pinPad.writeWKey(0, WorkKeyTypeEnum.PINKEY, pinKey, pinKey.size)
        }
        val pinPadInputListener = object : OnPinPadInputListener {
            override fun onInputResult(retCode: Int, data: ByteArray?) {
                if (retCode == SdkResult.Success || retCode == SdkResult.PinPad_No_Pin_Input || retCode == SdkResult.PinPad_Input_Cancel) {
                    if (data != null && isOnlinePin) {
                        val temp = ByteArray(8)
                        System.arraycopy(data, 0, temp, 0, 8)
//                        if (dukptConfig != null) {
//                            val pinBlock =
//                                pinPad.dukptEncrypt(0, DukptKeyModeEnum.REQUEST, data, data.size)
//                            cardData.pinBlock = pinBlock.hexString
//                            cardData.ksnData = pinPad.dukptCurrentKsn(0)?.hexString
//                        }
                    }
                    emvHandler2.onSetPinInputResponse(
                        retCode != SdkResult.PinPad_Input_Cancel,
                        retCode == SdkResult.PinPad_No_Pin_Input
                    )
                } else {
                    emvHandler2.onSetPinInputResponse(false, false)
                }
            }

            override fun onSendKey(keyCode: Byte) {

            }
        }
        activity.runOnUiThread {
            pinPad.setPinKeyboardMode(PinKeyboardModeEnum.RANDOM)
            if (isOnlinePin) {
                pinPad.inputOnlinePin(
                    intArrayOf(0x04), 60, cardNo.toByteArray(), 10,
                    PinAlgorithmModeEnum.ISO9564FMT1, pinPadInputListener
                )
            } else {
                pinPad.inputOfflinePin(intArrayOf(0x04), 60, pinPadInputListener)
            }
        }
    }

    override fun onContactlessTapCardAgain() {
    }

    override fun onOnlineProc() {
        val emvOnlineResult = EmvOnlineResultEntity()
        emvOnlineResult.authCode = "123450"
        emvOnlineResult.rejCode = "00"
        if (filed55 == null) filed55 = getFiled55String()
        emvOnlineResult.recvField55 = filed55!!.hexBytes
        emvHandler2.onSetOnlineProcResponse(SdkResult.Success, emvOnlineResult)
    }

    override fun onPrompt(prompt: PromptEnum?) {
        emvHandler2.onSetPromptResponse(true)
    }

    override fun onRemoveCard() {
        emvHandler2.onSetRemoveCardResponse()
    }

    override fun onFinish(retCode: Int, entity: EmvProcessResultEntity?) {
        when (retCode) {
            SdkResult.Emv_Success_Arpc_Fail, SdkResult.Success, SdkResult.Emv_Script_Fail -> {
                val cardDataInfo = emvHandler2.emvCardDataInfo
                cardData.apply {
                    pan = cardDataInfo.cardNo
                    track2 = cardDataInfo.tk2
                    src = cardDataInfo.serviceCode
//                    track1 = cardDataInfo.tk1
                }
                if (filed55 == null) filed55 = getFiled55String()
                cardData.mIccString = filed55!!

                cardData.apply {
                    status = CardTransactionStatus.Success
                    transactionAmount = emvHandler2.getValue(0x9F02)
//                    pan = emvHandler2.getValue(0x5A, hex = false, fPadded = true)
                    exp = emvHandler2.getValue(0x5F24)
                    holder = emvHandler2.getValue(0x5F20, true)
                    cardSequenceNumber = emvHandler2.getValue(0x5f34)
                    aid = emvHandler2.getValue(0x9F06)
                    track2 = emvHandler2.getValue(0x57, hex = false, fPadded = true)
                    var markerIndex = track2.indexOf("D")
                    if (markerIndex < 0) {
                        markerIndex = track2.indexOf("=")
                    }
                    src = track2.substring(markerIndex + 5, markerIndex + 8)

                    cardMethod = CardReaderEvent.CHIP
                }
                val tvr = emvHandler2.getTag(0x95)
                if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                    cardData.status = CardTransactionStatus.OfflinePinVerifyError
                }
            }
            SdkResult.Emv_Arpc_Fail, SdkResult.Emv_Declined -> {
                cardData.status = CardTransactionStatus.Failure
            }
            SdkResult.Emv_Cancel -> {
                cardData.status = CardTransactionStatus.UserCancel
            }
            SdkResult.Emv_Offline_Declined -> {
                cardData.status = CardTransactionStatus.Failure
            }
            SdkResult.Emv_Card_Block -> {
                cardData.status = CardTransactionStatus.OfflinePinVerifyError
            }
            SdkResult.Emv_Terminate -> {
                cardData.status = CardTransactionStatus.UserCancel
            }
            else -> {
                cardData.status = CardTransactionStatus.Failure
            }
        }

        continuation.resume(cardData)
    }

    private fun getFiled55String(): String {
        val tags = arrayOf(
            "82",
            "84",
            "95",
            "9F26",
            "9F27",
            "9F10",
            "9F37",
            "9F36",
            "9A",
            "9C",
            "9F02",
            "9F03",
            "5F2A",
            "9F1A",
            "9F03",
            "9F33",
            "9F34",
            "9F35",
            "9F09",
            "9F41"
        )
        return emvHandler2.getTlvByTags(tags)
    }

    private inline val ByteArray.encryptedPinBlock: ByteArray
        get() {
            val pin = String(this)
            val pan = emvHandler2.getValue(0x5A, hex = false, fPadded = true)
            val pinBlock = "0${pin.length}$pin".padEnd(16, 'F')
            val panBlock = pan.substring(3, pan.lastIndex).padStart(16, '0')
            val cipherKey = get<PosParameter>().pinKey.hexBytes
            val cryptData = pinBlock.hexBytes xor panBlock.hexBytes
            val tripleDesCipher = TripleDesCipher(cipherKey)
            return tripleDesCipher.encrypt(cryptData).copyOf(8)
        }
}