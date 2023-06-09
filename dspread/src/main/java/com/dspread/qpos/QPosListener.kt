package com.dspread.qpos

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Base64
import android.util.Log
import com.cluster.core.ui.widget.DialogConfirmParams
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.PinBlockHelper
import com.cluster.core.util.SharedPref
import com.cluster.core.util.format
import com.cluster.pos.PosManager
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardTransactionStatus
import com.cluster.pos.extensions.hexBytes
import com.dspread.R
import com.dspread.qpos.utils.DUKPK2009_CBC
import com.dspread.qpos.utils.TLV
import com.dspread.qpos.utils.TLVParser
import com.dspread.xpos.CQPOSService
import com.dspread.xpos.QPOSService
import java.time.Instant
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QPosListener(
    private val pos: QPOSService,
    private val qPosManager: QPosManager,
    private val defaultPosParameter: PosParameter
) : CQPOSService(){
    internal var cardDataContinuation: Continuation<CardData?>? = null
    private var cardEventContinuation: Continuation<CardReaderEvent>? = null
    internal var posConnectContinuation: Continuation<Boolean>? = null
    internal var unitContinuation: Continuation<Unit>? = null
    internal var stringContinuation: Continuation<String?>? = null
    private var cardData = QposCardData()
    private val pinBlockHelper = PinBlockHelper()
    private fun getString(id: Int) = qPosManager.activity.getString(id)
    private var cryptogramData = ""


    override fun onRequestWaitingUser() {}

    override fun onDoTradeResult(
        result: QPOSService.DoTradeResult,
        decodeData: Hashtable<String, String>?,
    ) {
        when (result) {
            QPOSService.DoTradeResult.NONE -> {
                statusEditText.setText(getString(R.string.no_card_detected))
            }
            QPOSService.DoTradeResult.ICC -> {
                statusEditText.setText(getString(R.string.icc_card_inserted))
                TRACE.d("EMV ICC Start")
                pos.doEmvApp(QPOSService.EmvOption.START_WITH_FORCE_PIN)
            }
            QPOSService.DoTradeResult.NOT_ICC -> {
                statusEditText.setText(getString(R.string.card_inserted))
            }
            QPOSService.DoTradeResult.BAD_SWIPE -> {
                statusEditText.setText(getString(R.string.bad_swipe))
            }
            QPOSService.DoTradeResult.MCR -> {
                decodeData ?: return
                extractMcrData(decodeData)
            }
            QPOSService.DoTradeResult.NO_RESPONSE -> {
                statusEditText.setText(getString(R.string.card_no_response))
            }
            else -> {}
        }
    }

    override fun onQposInfoResult(posInfoData: Hashtable<String, String>) {
        TRACE.d("onQposInfoResult$posInfoData")
        val isSupportedTrack1 = posInfoData["isSupportedTrack1"] ?: ""
        val isSupportedTrack2 = posInfoData["isSupportedTrack2"] ?: ""
        val isSupportedTrack3 = posInfoData["isSupportedTrack3"] ?: ""
        val bootloaderVersion = posInfoData["bootloaderVersion"] ?: ""
        val firmwareVersion = posInfoData["firmwareVersion"] ?: ""
        val isUsbConnected = posInfoData["isUsbConnected"] ?: ""
        val isCharging = posInfoData["isCharging"] ?: ""
        val batteryLevel = posInfoData["batteryLevel"] ?: ""
        val batteryPercentage = posInfoData["batteryPercentage"] ?: ""
        val hardwareVersion = posInfoData["hardwareVersion"] ?: ""
        val SUB = posInfoData["SUB"] ?: ""
        val pciFirmwareVersion = posInfoData["PCI_firmwareVersion"] ?: ""
        val pciHardwareVersion = posInfoData["PCI_hardwareVersion"] ?: ""
        var content = ""
        content += getString(R.string.bootloader_version) + bootloaderVersion + "\n"
        content += getString(R.string.firmware_version) + firmwareVersion + "\n"
        content += getString(R.string.usb) + isUsbConnected + "\n"
        content += getString(R.string.charge) + isCharging + "\n"
        //			if (batteryPercentage==null || "".equals(batteryPercentage)) {
        content += getString(R.string.battery_level) + batteryLevel + "\n"
        //			}else {
        content += getString(R.string.battery_percentage) + batteryPercentage + "\n"
        //			}
        content += getString(R.string.hardware_version) + hardwareVersion + "\n"
        content += "SUB : $SUB\n"
        content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n"
        content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n"
        content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n"
        content += "PCI FirmwareVresion:$pciFirmwareVersion\n"
        content += "PCI HardwareVersion:$pciHardwareVersion\n"
        statusEditText.setText(content)

        Log.d("CheckDUKPT", "This is the battery level $batteryLevel")
        Log.d("CheckDUKPT", "This is the battery percentage $batteryPercentage")
    }

    fun cleanup() {
        updateThread?.concelSelf()
    }

    override fun onRequestTransactionResult(transactionResult: QPOSService.TransactionResult) {
        qPosManager.hideProgressBar()
        if (transactionResult == QPOSService.TransactionResult.APPROVED) {
            TRACE.d("onRequestTransactionResult Result is here $transactionResult")
            handleCardData()
        } else {
            qPosManager.showError(transactionResult.getMessage(activity)) {
                onClose {
                    activity.finish()
                }
            }
        }
    }

    override fun onRequestBatchData(tlv: String) {
        TRACE.d("ICC交易结束")
        var content: String = getString(R.string.batch_data)
        TRACE.d("onRequestBatchData(String tlv):$tlv")
        content += tlv
        statusEditText.setText(content)
    }

    override fun onRequestTransactionLog(tlv: String) {
        TRACE.d("onRequestTransactionLog(String tlv):$tlv")
        var content: String = getString(R.string.transaction_log)
        content += tlv
        statusEditText.setText(content)
    }

    override fun onQposIdResult(posIdTable: Hashtable<String, String>) {
        TRACE.w("onQposIdResult():$posIdTable")
        val posId =
            if (posIdTable["posId"] == null) "" else posIdTable["posId"]!!
        val csn =
            if (posIdTable["csn"] == null) "" else posIdTable["csn"]!!
        val psamId = if (posIdTable["psamId"] == null) "" else posIdTable["psamId"]!!
        val NFCId = if (posIdTable["nfcID"] == null) "" else posIdTable["nfcID"]!!
        var content = ""
        content += getString(R.string.posId) + posId + "\n"
        content += "csn: $csn\n"
        content += "conn: " + pos.bluetoothState + "\n"
        content += "psamId: $psamId\n"
        content += "NFCId: $NFCId\n"
        statusEditText.setText(content)
    }

    override fun onRequestSelectEmvApp(appList: ArrayList<String>) {
        val options = appList.map { DialogOptionItem(it) }
        qPosManager.showOptions(getString(R.string.please_select_app), options) {
            onSubmit { position ->
                pos.selectEmvApp(position)
            }

            onClose {
                pos.cancelSelectEmvApp()
            }
        }
    }

    override fun onRequestSetAmount() {
        pos.setAmount(
            sessionData.amount.toString(),
            sessionData.cashBackAmount.toString(),
            "566",
            sessionData.transactionType.asQposTransactionType
        )
    }

    override fun onRequestIsServerConnected() {
        pos.isServerConnected(true)
    }

    override fun onRequestOnlineProcess(tlv: String) {
        if (isPinCanceled) {
            cardData.ret = CardTransactionStatus.UserCancel.code
            pos.sendOnlineProcessResult(null)
        } else {
            val str = "8A023030"
            Log.d("onRequest", "Here to check the tlv data 2 $tlv")
            extractData(tlv)
            qPosManager.hideProgressBar()
            pos.sendOnlineProcessResult(str)
        }
    }

    private fun extractData(tlv: String) {
        val list: List<TLV> = TLVParser.parse(tlv) // get the tag list
        Log.d("onRequest", "Here to check the tlv List $list")
        val tlvStrArr = arrayOf("c0", "c2", "c1", "c7")
        val c0Tlv: TLV? = TLVParser.searchTLV(list, tlvStrArr[0])
        val c2Tlv: TLV? = TLVParser.searchTLV(list, tlvStrArr[1])
        val c1Tlv: TLV? = TLVParser.searchTLV(list, tlvStrArr[2])
        val c7Tlv: TLV? = TLVParser.searchTLV(list, tlvStrArr[3])
        if (c0Tlv == null || c2Tlv == null) return
        val ksn: String = c0Tlv.value
        val dataStr: String = c2Tlv.value
        val data = DUKPK2009_CBC.getData(
            ksn,
            dataStr,
            DUKPK2009_CBC.Enum_key.DATA,
            DUKPK2009_CBC.Enum_mode.CBC
        )
        Log.d("onRequest", "The emv data is here $data")

        val decryptTest = com.dspread.qpos.injectKey.DUKPK2009_CBC.getData(
            ksn,
            dataStr,
            com.dspread.qpos.injectKey.DUKPK2009_CBC.Enum_key.DATA,
            com.dspread.qpos.injectKey.DUKPK2009_CBC.Enum_mode.CBC
        )

        Log.d("onRequest", "The decrypt test emv data is here $decryptTest")

        try {
            val datas: List<TLV> = TLVParser.parse(decryptTest) ?: return

            Log.d("onRequest", "The decrypted emv datas is here $datas")

            cardData.apply {
            ret = CardTransactionStatus.Success.code
            transactionAmount = list.getValue("9F02")
            pan = datas.getValue("5A", hex = false, fpadded = true)
            exp = list.getValue("5F24").substring(0, 4)
            holder = list.getValue("5F20", true)
            cardSequenceNumber = list.getValue("5f34")
            aid = list.getValue("9F06")
            track2 = datas.getValue("57", hex = false, fpadded = true)
            var markerIndex = track2.indexOf("D", ignoreCase = true)
            if (markerIndex < 0) {
                markerIndex = track2.indexOf("=")
            }
            src = track2.substring(markerIndex + 5, markerIndex + 8)
            atc = datas.getValue("9F36")
            cryptogram = datas.getValue("9F26")
            cryptogramInformationData = datas.getValue("9f27")
            terminalCapabilities = datas.getValue("9F33")
            terminalType = datas.getValue("9F35")
            iad = datas.getValue("9F10")
            tvr = datas.getValue("95")
            unpredictedNumber = datas.getValue("9F37")
            dedicatedFileName = datas.getValue("84")
            transactionDate = datas.getValue("9A")
            transactionType = datas.getValue("9C")
            cardHolderVerificationMethod = list.getValue("9F34")
            amountAuthorized = list.getValue("9F02")
            amountOther = list.getValue("9F03")


            //setIccTlv(datas)
            cardMethod = CardReaderEvent.CHIP
        }


            Log.d("onRequest", "here to check the card data $cardData")
            Log.d("onRequest", "The terminal country code is ${datas.getValue("9F1A")}")
            Log.d("onRequest", "The transaction currency code is ${datas.getValue("5F2A")}")

            val tvr = datas.getValue("95").hexBytes
            if (tvr.isNotEmpty() && tvr.last() == 1.toByte()) {
                cardData.ret = CardTransactionStatus.OfflinePinVerifyError.code
                return
            }

            val pinKsn: String? = c1Tlv?.value
            val pinBlock: String? = c7Tlv?.value

            if (pinKsn == null || pinBlock == null) {
                cardData.pinBlock = ""
                cardData.ksnData = ""
            } else {
                val getDecryptPin = com.dspread.qpos.injectKey.DUKPK2009_CBC.getData(
                    pinKsn,
                    pinBlock,
                    com.dspread.qpos.injectKey.DUKPK2009_CBC.Enum_key.PIN,
                    com.dspread.qpos.injectKey.DUKPK2009_CBC.Enum_mode.CBC
                )
                Log.d("onRequest", "The getDecrypt Pin here is $getDecryptPin")
                val storedKsn = SharedPref[activity, "ksn", ""]
                val incrementKsn = incrementKsn(activity, storedKsn!!)
                val workingKey = pinBlockHelper.getSessionKey(PosManager.IPEK, incrementKsn)
                val encryptedPinBlock = pinBlockHelper.desEncryptDukpt(workingKey, getDecryptPin)
                val transactionKsn = pinBlockHelper.generateTransKsn(incrementKsn)
                cardData.pinBlock = encryptedPinBlock
                Log.d("onRequest", "The pinblock here is ${cardData.pinBlock}")
                cardData.ksnData = transactionKsn?.uppercase()
                Log.d("onRequest", "The ksn data here is ${cardData.ksnData}")
            }
        }
        catch (e : Exception){
            Log.d("onRequest", "Here to check the exception ${e.printStackTrace()}")
        }
    }

    private fun incrementKsn(context: Context, iKsn: String) : String {
        var ksnValue = iKsn
        ksnValue = ksnValue.substring(0, ksnValue.length - 5)
        val counter = SharedPref[context, "ksnCounter", "0"]!!.toLong().plus(1)
        SharedPref[activity, "ksnCounter"] = counter.toString()
        if(counter > 99997){
            SharedPref[activity, "ksnCounter"] = PosManager.KsnCounter
        }
        ksnValue += counter.toString().padStart(5, '0')
        return ksnValue
    }

    override fun onRequestTime() {
        val terminalTime = Instant.now().format("yyyyMMddHHmmss")
        pos.sendTime(terminalTime)
    }

    override fun onRequestDisplay(displayMsg: QPOSService.Display) {
        TRACE.d("onRequestDisplay(Display displayMsg):$displayMsg")
        var msg = ""
        if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
            msg = ""
        } else if (displayMsg == QPOSService.Display.PLEASE_WAIT) {
            msg = getString(R.string.wait)
        } else if (displayMsg == QPOSService.Display.REMOVE_CARD) {
            msg = getString(R.string.remove_card)
        } else if (displayMsg == QPOSService.Display.TRY_ANOTHER_INTERFACE) {
            msg = getString(R.string.try_another_interface)
        } else if (displayMsg == QPOSService.Display.PROCESSING) {
            msg = getString(R.string.processing)
        } else if (displayMsg == QPOSService.Display.INPUT_PIN_ING) {
            msg = "please input pin on pos"
        } else if (displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == QPOSService.Display.INPUT_LAST_OFFLINE_PIN) {
            msg = "please input offline pin on pos"
        } else if (displayMsg == QPOSService.Display.MAG_TO_ICC_TRADE) {
            msg = "please insert chip card on pos"
        } else if (displayMsg == QPOSService.Display.CARD_REMOVED) {
            msg = "card removed"
        }
        if (msg.isNotBlank()) qPosManager.showProgressBar(msg)
    }

    override fun onRequestFinalConfirm() {
        if (!isPinCanceled) {
            var message: String = getString(R.string.amount) + ": NGN" + sessionData.amount
            if (sessionData.cashBackAmount != 0L) {
                message += "\n" + getString(R.string.cashback_amount) + ": NGN" + sessionData.cashBackAmount
            }
            val params = DialogConfirmParams(
                getString(R.string.confirm_amount),
                message
            )
            qPosManager.confirm(params) {
                onSubmit {
                    pos.finalConfirm(true)
                }

                onClose {
                    pos.finalConfirm(false)
                }
            }
        } else {
            pos.finalConfirm(false)
        }
    }

    override fun onRequestNoQposDetected() {
        handleConnectionStatus(false)
    }

    override fun onRequestQposConnected() {
        pos.getQposId()
        handleConnectionStatus(true)
    }

    override fun onRequestQposDisconnected() {
        handleConnectionStatus(false)
    }

    override fun onError(errorState: QPOSService.Error) {
        updateThread?.concelSelf()
        TRACE.d("onError$errorState")
        qPosManager.hideProgressBar()
        when (errorState) {
            QPOSService.Error.CMD_NOT_AVAILABLE -> {
                statusEditText.setText(getString(R.string.command_not_available))
            }
            QPOSService.Error.TIMEOUT -> {
                statusEditText.setText(getString(R.string.device_no_response))
            }
            QPOSService.Error.DEVICE_RESET -> {
                statusEditText.setText(getString(R.string.device_reset))
            }
            QPOSService.Error.UNKNOWN -> {
                statusEditText.setText(getString(R.string.unknown_error))
            }
            QPOSService.Error.DEVICE_BUSY -> {
                statusEditText.setText(getString(R.string.device_busy))
            }
            QPOSService.Error.INPUT_OUT_OF_RANGE -> {
                statusEditText.setText(getString(R.string.out_of_range))
            }
            QPOSService.Error.INPUT_INVALID_FORMAT -> {
                statusEditText.setText(getString(R.string.invalid_format))
            }
            QPOSService.Error.INPUT_ZERO_VALUES -> {
                statusEditText.setText(getString(R.string.zero_values))
            }
            QPOSService.Error.INPUT_INVALID -> {
                statusEditText.setText(getString(R.string.input_invalid))
            }
            QPOSService.Error.CASHBACK_NOT_SUPPORTED -> {
                statusEditText.setText(getString(R.string.cashback_not_supported))
            }
            QPOSService.Error.CRC_ERROR -> {
                statusEditText.setText(getString(R.string.crc_error))
            }
            QPOSService.Error.COMM_ERROR -> {
                statusEditText.setText(getString(R.string.comm_error))
            }
            QPOSService.Error.MAC_ERROR -> {
                statusEditText.setText(getString(R.string.mac_error))
            }
            QPOSService.Error.APP_SELECT_TIMEOUT -> {
                statusEditText.setText(getString(R.string.app_select_timeout_error))
            }
            QPOSService.Error.CMD_TIMEOUT -> {
                statusEditText.setText(getString(R.string.cmd_timeout))
            }
            QPOSService.Error.ICC_ONLINE_TIMEOUT -> {
                if (pos == null) return
                pos.resetPosStatus()
                statusEditText.setText(getString(R.string.device_reset))
            }
            else -> {}
        }
        handleCardData()
        handleCardReaderEvent(CardReaderEvent.CANCELLED)
    }

    override fun onReturnReversalData(tlv: String) {
        var content: String = getString(R.string.reversal_data)
        content += tlv
        TRACE.d("onReturnReversalData(): $tlv")
        statusEditText.setText(content)
    }

    override fun onReturnGetPinResult(result: Hashtable<String, String>) {
        TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):$result")
        val pinBlock = result["pinBlock"]
        val pinKsn = result["pinKsn"]
        var content = "get pin result\n"
        content += getString(R.string.pinKsn) + " " + pinKsn + "\n"
        content += getString(R.string.pinBlock) + " " + pinBlock + "\n"
        statusEditText.setText(content)
        TRACE.i(content)
    }

    override fun onReturnApduResult(arg0: Boolean, arg1: String, arg2: Int) {
        TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2)
    }

    override fun onReturnPowerOffIccResult(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onReturnPowerOffIccResult(boolean arg0):$arg0")
    }

    override fun onReturnPowerOnIccResult(arg0: Boolean, arg1: String, arg2: String, arg3: Int) {
        TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3)
        if (arg0) {
            pos.sendApdu("123456")
        }
    }

    override fun onReturnSetSleepTimeResult(isSuccess: Boolean) {
        TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):$isSuccess")
        var content = ""
        content = if (isSuccess) {
            "set the sleep time success."
        } else {
            "set the sleep time failed."
        }
        statusEditText.setText(content)
    }

    override fun onGetCardNoResult(cardNo: String?) {
        handleStringContinuation(cardNo)
    }

    override fun onRequestCalculateMac(calMac: String?) {
//        calMac?.toByteArray()?.hexString
    }

    override fun onRequestSignatureResult(arg0: ByteArray) {
        TRACE.d("onRequestSignatureResult(byte[] arg0):$arg0")
    }

    override fun onRequestUpdateWorkKeyResult(result: QPOSService.UpdateInformationResult) {
        TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):$result")
    }

    override fun onReturnCustomConfigResult(
        isSuccess: Boolean,
        result: String,
    ) {
        handleUnitContinuation()
    }

    override fun onRequestSetPin() {
        TRACE.d("onRequestSetPin()")

        qPosManager.requestPIN("Enter PIN") {
            onSubmit { pin ->
                if (pin.length in 4..12) {
//                    if (pin == "000000") {
//                        pos.sendEncryptPin(
//                            0,
//                            "00000510F3C36060",
//                            "00000510F3C360600001",
//                            "5516422217375116"
//                        )
//                    } else {
                    Log.d("System.out", "Here to check the pin entered on the device : $pin")
                    pos.sendPin(pin)
//                    }
                } else {
                    isPinCanceled = true
                    pos.cancelPin()
                }
            }

            onClose {
                isPinCanceled = true
                pos.cancelPin()
            }
        }
    }

    override fun onReturnSetMasterKeyResult(isSuccess: Boolean) {
        TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : $isSuccess")
        statusEditText.setText("result: $isSuccess")
    }

    override fun onReturnBatchSendAPDUResult(batchAPDUResult: LinkedHashMap<Int, String>) {
        TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):$batchAPDUResult")
        val sb = StringBuilder()
        sb.append("APDU Responses: \n")
        for ((key, value) in batchAPDUResult) {
            sb.append("[$key]: $value\n")
        }
        statusEditText.setText("\n" + sb.toString())
    }

    override fun onBluetoothBondFailed() {
        TRACE.d("onBluetoothBondFailed()")
        statusEditText.setText("bond failed")
    }

    override fun onBluetoothBondTimeout() {
        TRACE.d("onBluetoothBondTimeout()")
        statusEditText.setText("bond timeout")
    }

    override fun onBluetoothBonded() {
        TRACE.d("onBluetoothBonded()")
        statusEditText.setText("bond success")
    }

    override fun onBluetoothBonding() {
        TRACE.d("onBluetoothBonding()")
        statusEditText.setText("bonding .....")
    }

    override fun onReturniccCashBack(result: Hashtable<String, String>) {
        TRACE.d("onReturniccCashBack(Hashtable<String, String> result):$result")
        var s = "serviceCode: " + result["serviceCode"]
        s += "\n"
        s += "trackblock: " + result["trackblock"]
        statusEditText.setText(s)
    }

    override fun onLcdShowCustomDisplay(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onLcdShowCustomDisplay(boolean arg0):$arg0")
    }

    override fun onUpdatePosFirmwareResult(arg0: QPOSService.UpdateInformationResult) {
        TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):$arg0")
        if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
            updateThread?.concelSelf()
        }
        statusEditText.setText("onUpdatePosFirmwareResult$arg0")
    }

    override fun onReturnDownloadRsaPublicKey(map: HashMap<String, String>?) {
        TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):$map")
        if (map == null) {
            TRACE.d("MainActivity++++++++++++++map == null")
            return
        }
        val randomKeyLen = map["randomKeyLen"]
        val randomKey = map["randomKey"]
        val randomKeyCheckValueLen = map["randomKeyCheckValueLen"]
        val randomKeyCheckValue = map["randomKeyCheckValue"]
        TRACE.d("randomKey$randomKey    \n    randomKeyCheckValue$randomKeyCheckValue")
        statusEditText.setText(
            "randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:"
                    + randomKeyCheckValue
        )
    }

    override fun onGetPosComm(
        mod: Int,
        amount: String,
        posid: String,
    ) {
        TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid)
    }

    override fun onPinKey_TDES_Result(arg0: String) {
        TRACE.d("onPinKey_TDES_Result(String arg0):$arg0")
        statusEditText.setText("result:$arg0")
    }

    override fun onUpdateMasterKeyResult(
        arg0: Boolean,
        arg1: Hashtable<String, String>,
    ) { // TODO Auto-generated method stub
        TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString())
    }

    override fun onEmvICCExceptionData(arg0: String) { // TODO Auto-generated method stub
        TRACE.d("onEmvICCExceptionData(String arg0):$arg0")
    }

    override fun onSetParamsResult(
        arg0: Boolean,
        arg1: Hashtable<String, Any>,
    ) { // TODO Auto-generated method stub
        TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString())
    }

    override fun onGetInputAmountResult(
        arg0: Boolean,
        arg1: String,
    ) { // TODO Auto-generated method stub
        TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1)
    }

    override fun onCbcMacResult(result: String?) {
        TRACE.d("onCbcMacResult(String result):$result")
        if (result == null || "" == result) {
            statusEditText.setText("cbc_mac:false")
        } else {
            statusEditText.setText("cbc_mac: $result")
        }
    }

    override fun onConfirmAmountResult(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onConfirmAmountResult(boolean arg0):$arg0")
    }

    override fun onQposIsCardExist(cardIsExist: Boolean) {
        TRACE.d("onQposIsCardExist(boolean cardIsExist):$cardIsExist")
        if (cardIsExist) {
            statusEditText.setText("cardIsExist:$cardIsExist")
        } else {
            statusEditText.setText("cardIsExist:$cardIsExist")
        }
    }

    override fun onSetBuzzerResult(arg0: Boolean) {
        TRACE.d("onSetBuzzerResult(boolean arg0):$arg0")
        if (arg0) {
            statusEditText.setText("蜂鸣器设置成功")
        } else {
            statusEditText.setText("蜂鸣器设置失败")
        }
    }

    override fun onSetBuzzerTimeResult(b: Boolean) {
        TRACE.d("onSetBuzzerTimeResult(boolean b):$b")
    }

    override fun onSetBuzzerStatusResult(b: Boolean) {
        TRACE.d("onSetBuzzerStatusResult(boolean b):$b")
    }

    override fun onGetBuzzerStatusResult(s: String) {
        TRACE.d("onGetBuzzerStatusResult(String s):$s")
    }

    override fun onSetManagementKey(arg0: Boolean) {
        TRACE.d("onSetManagementKey(boolean arg0):$arg0")
        if (arg0) {
            statusEditText.setText("设置主密钥成功")
        } else {
            statusEditText.setText("设置主密钥失败")
        }
    }

    override fun onReturnUpdateIPEKResult(arg0: Boolean) {
        Log.d("System.out","Check IPEK update response here : $arg0")
        TRACE.d("onReturnUpdateIPEKResult(boolean arg0):$arg0")
        handleUnitContinuation()
    }

    override fun onReturnUpdateEMVRIDResult(arg0: Boolean) {
        TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):$arg0")
        handleUnitContinuation()
    }

    override fun onReturnUpdateEMVResult(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onReturnUpdateEMVResult(boolean arg0) : $arg0")
        handleUnitContinuation()
    }

    override fun onBluetoothBoardStateResult(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onBluetoothBoardStateResult(boolean arg0):$arg0")
    }

    override fun onRequestUpdateKey(arg0: String) { // TODO Auto-generated method stub
        TRACE.d("onRequestUpdateKey(String arg0):$arg0")
        statusEditText.setText("update checkvalue : $arg0")
    }

    override fun onReturnGetQuickEmvResult(arg0: Boolean) { // TODO Auto-generated method stub
        TRACE.d("onReturnGetQuickEmvResult(boolean arg0):$arg0")
        if (arg0) {
            statusEditText.setText("emv已配置")
            pos.isQuickEmv = true
        } else {
            statusEditText.setText("emv未配置")
        }
    }

    override fun onQposDoGetTradeLogNum(arg0: String) {
        TRACE.d("onQposDoGetTradeLogNum(String arg0):$arg0")
        val a = arg0.toInt(16)
        if (a >= 188) {
            statusEditText.setText("the trade num has become max value!!")
            return
        }
        statusEditText.setText("get log num:$a")
    }

    override fun onQposDoTradeLog(arg0: Boolean) {}

    override fun onAddKey(arg0: Boolean) {}

    override fun onQposKsnResult(arg0: Hashtable<String, String>) {}

    override fun onQposDoGetTradeLog(arg0: String, arg1: String) {}

    private fun getPermissionDeviceList(): List<UsbDevice> {
        val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
        return usbManager.deviceList.values.map { it }
    }

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Companion.ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false
                        )
                    ) {
                        if (device != null) {
                            // call method to set up device communication
                            TRACE.i("usbpermission granted for device $device")
                            pos.setPermissionDevice(device)
                        }
                    } else {
                        TRACE.i("usbpermission denied for device $device");
                    }
                    activity.unregisterReceiver(this)
                }
            }
        }
    }

    private fun devicePermissionRequest(
        mManager: UsbManager,
        usbDevice: UsbDevice,
    ) {
        val mPermissionIntent = PendingIntent.getBroadcast(
            activity, 0, Intent(
                "com.android.example.USB_PERMISSION"
            ), 0
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        activity.registerReceiver(mUsbReceiver, filter)
        mManager.requestPermission(usbDevice, mPermissionIntent)
    }

    override fun onRequestDevice() {
        val deviceList: List<UsbDevice> = getPermissionDeviceList()
        val mManager =
            activity.getSystemService(Context.USB_SERVICE) as UsbManager
        for (i in deviceList.indices) {
            val usbDevice = deviceList[i]
            if (usbDevice.vendorId == 2965 || usbDevice.vendorId == 0x03EB) {
                if (mManager.hasPermission(usbDevice)) {
                    pos.setPermissionDevice(usbDevice)
                } else {
                    devicePermissionRequest(mManager, usbDevice)
                }
            }
        }
    }

    override fun onGetKeyCheckValue(checkValue: List<String>?) {
        if (checkValue != null) {
            val buffer = StringBuffer()
            buffer.append("{")
            for (i in checkValue.indices) {
                buffer.append(checkValue[i]).append(",")
            }
            buffer.append("}")
            statusEditText.setText(buffer.toString())
        }
    }

    override fun onGetDevicePubKey(clearKeys: String) {
        TRACE.d("onGetDevicePubKey(clearKeys):$clearKeys")
        statusEditText.setText(clearKeys)
        val lenStr = clearKeys.substring(0, 4)
        var sum = 0
        for (i in 0..3) {
            val bit = lenStr.substring(i, i + 1).toInt()
            sum += bit * Math.pow(16.0, (3 - i).toDouble()).toInt()
        }
//        val pubModel = clearKeys.substring(4, 4 + sum * 2)
//        if (resetIpekFlag || resetMasterKeyFlag) pos.updateKeys(activity, pubModel, configService)
    }

    override fun onTradeCancelled() {
        TRACE.d("onTradeCancelled")
        qPosManager.hideProgressBar()
    }

    override fun onReturnSetAESResult(
        isSuccess: Boolean,
        result: String,
    ) {
    }

    override fun onReturnAESTransmissonKeyResult(
        isSuccess: Boolean,
        result: String,
    ) {
    }

    override fun onReturnSignature(
        b: Boolean,
        signaturedData: String,
    ) {
        if (b) {
            val encode = Base64.encode(signaturedData.toByteArray(), Base64.DEFAULT)
            statusEditText.setText("signature data (Base64 encoding):$encode")
        }
    }

    override fun onReturnConverEncryptedBlockFormat(result: String) {
        statusEditText.setText(result)
    }

    override fun onQposDoSetRsaPublicKey(arg0: Boolean) {}

    override fun onQposGenerateSessionKeysResult(arg0: Hashtable<String, String>?) {
        if (arg0 != null) {
            TRACE.d("onQposGenerateSessionKeysResult(Hashtable<String, String> arg0):$arg0")
            val rsaFileName = arg0["rsaReginString"]
            val enPinKeyData = arg0["enPinKey"]
            val enKcvPinKeyData = arg0["enPinKcvKey"]
            val enCardKeyData = arg0["enDataCardKey"]
            val enKcvCardKeyData = arg0["enKcvDataCardKey"]
            statusEditText.setText(
                "rsaFileName:" + rsaFileName + "\nenPinKeyData:" + enPinKeyData + "\nenKcvPinKeyData:" +
                        enKcvPinKeyData + "\nenCardKeyData:" + enCardKeyData + "\nenKcvCardKeyData:" + enKcvCardKeyData
            )
        } else {
            statusEditText.setText("get key failed,pls try again!")
        }
    }

    override fun transferMifareData(arg0: String) {}

    override fun onReturnRSAResult(arg0: String?) {
        TRACE.d("onReturnRSAResult(String arg0):$arg0")
        if (arg0 != null) {
            statusEditText.setText("rsa data:\n$arg0")
        } else {
            statusEditText.setText("get the rsa failed")
        }
    }

    override fun onRequestNoQposDetectedUnbond() { // TODO Auto-generated method stub
        TRACE.d("onRequestNoQposDetectedUnbond()")
    }

    private fun handleConnectionStatus(status: Boolean) {
        val continuation = posConnectContinuation ?: return
        posConnectContinuation = null
        continuation.resume(status)
    }

    private fun handleUnitContinuation() {
        val continuation = unitContinuation ?: return
        unitContinuation = null
        continuation.resume(Unit)
    }

    private fun handleStringContinuation(string: String?) {
        val continuation = stringContinuation ?: return
        stringContinuation = null
        continuation.resume(string)
    }

    private fun handleCardData() {
        val continuation = cardDataContinuation ?: return
        cardDataContinuation = null
        continuation.resume(cardData)
    }

    private fun handleCardReaderEvent(cardReaderEvent: CardReaderEvent) {
        val continuation = cardEventContinuation ?: return
        cardEventContinuation = null
        continuation.resume(cardReaderEvent)
    }

    internal suspend inline fun waitForUnit(crossinline block: () -> Unit) =
        suspendCoroutine<Unit> {
            unitContinuation = it
            block()
        }

    internal suspend inline fun waitForString(crossinline block: () -> Unit?) =
        suspendCoroutine<String?> {
            stringContinuation = it
            block()
        }

    //////////// STUBS /////////////

    private var isPinCanceled = false
    private val activity get() = qPosManager.activity
    private val sessionData get() = qPosManager.sessionData
    private val statusEditText = object {
        fun setText(string: String?) {
            TRACE.d(string)
        }
    }
    private val updateThread: UpdateThread? = UpdateThread(qPosManager)

    companion object {
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}