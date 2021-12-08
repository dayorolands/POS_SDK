package com.nexgo.n3

import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debug
import com.cluster.core.util.debugOnly
import com.cluster.core.util.toCurrencyFormat
import com.cluster.pos.EmvException
import com.cluster.pos.PosManager
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.hexString
import com.cluster.pos.utils.asDesEdeKey
import com.cluster.pos.utils.encrypt
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nexgo.R
import com.nexgo.databinding.NexgoN3PinInputDialogBinding
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.pinpad.*
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.emv.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class N3EmvListener(
    private val activity: CreditClubActivity,
    private val deviceEngine: DeviceEngine,
    private val sessionData: PosManager.SessionData,
    private val continuation: Continuation<CardData?>,
    private val defaultPosParameter: PosParameter,
) : OnEmvProcessListener2 {
    private var hasResumed: Boolean = false
    private val emvHandler2 = deviceEngine.getEmvHandler2("app2")
    private val cardData = N3CardData()
    private var filed55: String? = null
    private val prefs = activity.getEncryptedSharedPreferences("com.nexgo.n3.manager")

    override fun onSelApp(
        appNameList: List<String>?,
        appInfoList: List<CandidateAppInfoEntity>?,
        isFirstSelect: Boolean,
    ) {
        appNameList ?: return
        emvHandler2.onSetSelAppResponse(1)
    }

    override fun onTransInitBeforeGPO() {
        emvHandler2.setPureKernelCapab(byteArrayOf(0xE0.toByte(), 0x40.toByte(), 0xC8.toByte()))
        emvHandler2.onSetTransInitBeforeGPOResponse(true)
    }

    override fun onConfirmCardNo(cardInfo: CardInfoEntity) {
        emvHandler2.onSetConfirmCardNoResponse(true)
    }

    override fun onCardHolderInputPin(isOnlinePin: Boolean, leftTimes: Int) {
        if (!isOnlinePin && leftTimes < 1) {
            emvHandler2.emvProcessCancel()
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(N3CardData().apply {
                    status = CardTransactionStatus.CardRestricted
                })
            }
            return
        }

        val pinPad: PinPad = deviceEngine.pinPad
        val cardNo = emvHandler2.emvCardDataInfo.cardNo
        val amount = sessionData.amount / 100.0
        var amountText = ""
        if (sessionData.amount > 0) amountText = "Amount: ${amount.toCurrencyFormat()}"
        if (sessionData.cashBackAmount > 0) amountText = "$amount        " +
                "Cashback Amount: ${amount.toCurrencyFormat()}"
        val dukptConfig = sessionData.getDukptConfig?.invoke(cardNo, amount)
        val posParameter: PosParameter =
            sessionData.getPosParameter?.invoke(cardNo, sessionData.amount / 100.0)
                ?: defaultPosParameter
        if (dukptConfig != null) {
            val ipekBytes = dukptConfig.ipek.hexBytes
            val ksnBytes = dukptConfig.ksn.hexBytes

            val cipherKey = dukptConfig.ipek.padEnd(32, '0').hexBytes.asDesEdeKey
            val oldKcv = prefs.getString("kcv", null)
            val newKcv = cipherKey.encrypt(ByteArray(8)).hexString
            pinPad.setAlgorithmMode(AlgorithmModeEnum.DUKPT)
            if (oldKcv != newKcv) {
                val result = pinPad.dukptKeyInject(
                    0,
                    DukptKeyTypeEnum.IPEK,
                    ipekBytes,
                    ipekBytes.size,
                    ksnBytes,
                )
                debug("Dukpt inject result is $result")
                if (result == 0) prefs.edit { putString("kcv", newKcv) }
            }
            pinPad.dukptKsnIncrease(0)
        } else {
            pinPad.setAlgorithmMode(AlgorithmModeEnum.DES)
            val masterKey = posParameter.masterKey.hexBytes
            pinPad.writeMKey(0, masterKey, masterKey.size)
            val tempPinKey = posParameter.pinKey.hexBytes
            val pinKey = ByteArray(24)
            System.arraycopy(tempPinKey, 0, pinKey, 0, 16)
            System.arraycopy(tempPinKey, 0, pinKey, 16, 8)

            pinPad.writeWKey(0, WorkKeyTypeEnum.PINKEY, pinKey, pinKey.size)
        }
        activity.runOnUiThread {
            var pwdText = ""
            val inflater = activity.layoutInflater
            val binding =
                DataBindingUtil.inflate<NexgoN3PinInputDialogBinding>(
                    inflater,
                    R.layout.nexgo_n3_pin_input_dialog,
                    null,
                    false
                )
            binding.triesLeftTv.text = "$leftTimes"
            binding.amountTv.text = amountText
            val dialog = AlertDialog.Builder(activity).setView(binding.root).create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val pinPadInputListener = object : OnPinPadInputListener {
                override fun onInputResult(retCode: Int, data: ByteArray?) {
                    dialog.dismiss()
                    if (retCode == SdkResult.Success || retCode == SdkResult.PinPad_No_Pin_Input || retCode == SdkResult.PinPad_Input_Cancel) {
                        if (retCode == SdkResult.Success && data != null && isOnlinePin) {
                            if (dukptConfig != null) {
                                cardData.pinBlock = data.hexString
                                cardData.ksnData = pinPad.dukptCurrentKsn(0)?.hexString
                            } else {
                                val secretKey = posParameter.pinKey.hexBytes.asDesEdeKey
                                val pinPanXor = pinPad.desByWKey(
                                    0,
                                    WorkKeyTypeEnum.PINKEY,
                                    data,
                                    data.size,
                                    DesKeyModeEnum.KEY_ALL,
                                    CalcModeEnum.DECRYPT,
                                )
                                val encryptedPinBlock = secretKey.encrypt(pinPanXor).copyOf(8)
                                cardData.pinBlock = encryptedPinBlock.hexString
                            }
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
                    activity.runOnUiThread {
                        if (keyCode == PinPadKeyCode.KEYCODE_CLEAR) {
                            pwdText = ""
                        } else {
                            pwdText += "* "
                        }
                        binding.pinTv.text = pwdText
                    }
                }
            }

            pinPad.setPinKeyboardMode(PinKeyboardModeEnum.RANDOM)
            if (isOnlinePin) {
                pinPad.inputOnlinePin(
                    intArrayOf(0x04),
                    60,
                    cardNo.toByteArray(),
                    0,
                    PinAlgorithmModeEnum.ISO9564FMT1,
                    pinPadInputListener,
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
        emvOnlineResult.recvField55 = getFiled55String().hexBytes
        emvHandler2.onSetOnlineProcResponse(SdkResult.Success, emvOnlineResult)
    }

    override fun onPrompt(prompt: PromptEnum?) {
        emvHandler2.onSetPromptResponse(true)
    }

    override fun onRemoveCard() {
        emvHandler2.onSetRemoveCardResponse()
    }

    override fun onFinish(retCode: Int, entity: EmvProcessResultEntity?) {
        if (retCode != SdkResult.Success) {
            val exception = EmvException("Nexgo EMV failed with ret $retCode")
            debugOnly { Log.e("N3", exception.message, exception) }
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
        when (retCode) {
            SdkResult.Emv_Success_Arpc_Fail, SdkResult.Success, SdkResult.Emv_Script_Fail -> {
                val cardDataInfo = emvHandler2.emvCardDataInfo
                cardData.mIccString = getFiled55String()
                cardData.apply {
                    pan = cardDataInfo.cardNo
                    track2 = cardDataInfo.tk2
                    src = cardDataInfo.serviceCode
//                    track1 = cardDataInfo.tk1
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

        if (!hasResumed) {
            hasResumed = true
            continuation.resume(cardData)
        }
    }

    private fun getFiled55String(): String {
        if (filed55 != null) return filed55!!
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
        filed55 = emvHandler2.getTlvByTags(tags)
        return filed55!!
    }
}