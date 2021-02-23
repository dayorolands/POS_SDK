package com.dspread.qpos

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.format
import com.creditclub.pos.DukptConfig
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosManager
import com.creditclub.pos.card.*
import com.creditclub.pos.utils.TripleDesCipher
import com.dspread.qpos.utils.FileUtils
import com.dspread.qpos.utils.hexBytes
import com.dspread.qpos.utils.hexString
import com.dspread.xpos.QPOSService
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Instant
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class QPosCardReader(
    private val activity: CreditClubActivity,
    private val pos: QPOSService,
    private val listener: QPosListener,
    private val mainScope: CoroutineScope,
    private val sessionData: PosManager.SessionData
) : CardReader, KoinComponent {

    private val keyIndex = 0
    private val posConfig: PosConfig by inject()
    private var watchJob: Job? = null
    private val dialogProvider = activity.dialogProvider

    override suspend fun waitForCard(): CardReaderEvent {
        val canUseBluetooth = checkBluetoothPermission()
        if (!canUseBluetooth) return CardReaderEvent.CANCELLED
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled && !turnOnBluetooth()) {
            dialogProvider.showErrorAndWait("Could not turn on bluetooth")
            return CardReaderEvent.CANCELLED
        }

        Looper.myLooper() ?: Looper.prepare()
        val handler = Handler(Looper.myLooper()!!)
        pos.initListener(handler, listener)
        pos.clearBluetoothBuffer()
        pos.setTerminalID(posConfig.terminalId, 5000)
        val device = findDevice() ?: return CardReaderEvent.CANCELLED
        val connected: Boolean = connectDevice(device)
        if (!connected) {
            dialogProvider.showErrorAndWait("Could not connect to device")
            return CardReaderEvent.CANCELLED
        }

        return CardReaderEvent.CHIP
    }

    private suspend fun turnOnBluetooth(): Boolean {
        val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val result = ActivityResultManager.getActivityResult(activity, turnOn, 2012)
        return result.resultCode == Activity.RESULT_OK
    }

    private suspend fun checkBluetoothPermission(): Boolean {
        val permissionResult = PermissionManager.requestPermissions(
            activity,
            2000,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
        )

        when (permissionResult) {
            is PermissionResult.PermissionGranted -> {
                return true
            }
            is PermissionResult.PermissionDenied -> {
                dialogProvider.showErrorAndWait("Bluetooth access is required to use mPOS")
            }
            is PermissionResult.PermissionDeniedPermanently -> {
                dialogProvider.showErrorAndWait(
                    "Bluetooth access is required to use mPOS.\n" +
                            "Please manually go to settings and enable permission(s)"
                )
            }
            is PermissionResult.ShowRational -> {
                dialogProvider.showErrorAndWait("Bluetooth access is required to use mPOS")
            }
        }

        return false
    }

    override suspend fun read(amountStr: String): CardData? {
        dialogProvider.showProgressBar("Securing Connection")
        updateEmvConfig()
        dialogProvider.hideProgressBar()
        val terminalTime = Instant.now().format("HHmmss")
        val cardNo = listener.waitForString { pos.getIccCardNo(terminalTime) } ?: return null
        if (cardNo.isBlank()) return QposCardData().apply { status = CardTransactionStatus.Failure }
        loadKeys(cardNo)
        return suspendCoroutine { continuation ->
            listener.cardDataContinuation = continuation
            pos.doTrade(keyIndex, 60)
        }
    }

    override fun endWatch() {
        watchJob?.cancel()
        watchJob = null
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        watchJob = mainScope.launch {
            val connected = suspendCoroutine<Boolean> { continuation ->
                listener.posConnectContinuation = continuation
            }
            watchJob = null
            if (!connected) onEventChange(CardReaderEvent.Timeout)
        }
    }

    private suspend fun updateEmvConfig() {
//        val emvAppCfg = FileUtils.readAssetsLine("emv_app.bin", activity)?.hexString
//        val emvCapkCfg = FileUtils.readAssetsLine("emv_capk.bin", activity)?.hexString
        val emvProfileTlv = String(FileUtils.readAssetsLine("emv_profile_tlv.xml", activity))

//        listener.waitForUnit { pos.updateEmvConfig(emvAppCfg, emvCapkCfg) }
        listener.waitForUnit { pos.updateEMVConfigByXml(emvProfileTlv) }
//        for (emvAid in posParameter.emvAids) {
//            listener.waitForUnit {
//                pos.updateEmvAPPByTlv(QPOSService.EMVDataOperation.update, emvAid)
//            }
//        }
//        for (capKey in posParameter.capKeys) {
//            listener.waitForUnit {
//                pos.updateEmvCAPKByTlv(QPOSService.EMVDataOperation.update, capKey)
//            }
//        }
    }

    private suspend fun loadKeys(cardNo: String) {
        val amount = sessionData.amount / 100.0
        val dukptConfig = sessionData.getDukptConfig?.invoke(cardNo, amount)
        val ksn = "09118012400705E00000"
        val ipekString = "C22766F7379DD38AA5E1DA8C6AFA75AC"
        val kcvString = "B2DE27F60A443944" /*Dukpt.encryptTripleDes(ipek, ByteArray(8)).hexString*/

        listener.dukptConfig = dukptConfig
        listener.waitForUnit {
            pos.updateIPEKOperationByKeyType(
                "01",
                ksn,
                ipekString,
                kcvString,
                ksn,
                ipekString,
                kcvString,
                dukptConfig?.ksn ?: "09118012400705E00000",
                dukptConfig?.ipek ?: "C22766F7379DD38AA5E1DA8C6AFA75AC",
                dukptConfig?.kcv ?: "B2DE27F60A443944"
            )
        }
    }

    private inline val DukptConfig.kcv: String
        get() = TripleDesCipher(ipek.hexBytes).encrypt(ByteArray(8)).hexString
//        get() = Dukpt.encryptTripleDes(ipek.hexBytes, ByteArray(8)).hexString

    private suspend fun connectDevice(device: BluetoothDevice): Boolean {
        dialogProvider.showProgressBar("Connecting to mPOS device")
        val connected: Boolean = withContext(Dispatchers.IO) {
            pos.syncConnectBluetooth(true, 25, device.address)
        }
        dialogProvider.hideProgressBar()
        return connected
    }

    private suspend fun findDevice(): BluetoothDevice? = suspendCoroutine {
        pos.scanQPos2Mode(activity, 20)
        var findDeviceDialog: QPosFindDeviceDialog? = null
        var timerTask: TimerTask? = null
        findDeviceDialog = QPosFindDeviceDialog(activity) {
            onSubmit { device ->
                timerTask?.cancel()
                pos.stopScanQPos2Mode()
                findDeviceDialog?.dismiss()
                findDeviceDialog = null
                it.resume(device)
            }

            onClose {
                timerTask?.cancel()
                pos.stopScanQPos2Mode()
                it.resume(null)
            }
        }
        timerTask = Timer().scheduleAtFixedRate(1000, 1000) {
            activity.runOnUiThread {
                findDeviceDialog?.updateList(
                    pos.deviceList?.filter { it.name.startsWith("MPOS") }
                )
            }
        }
        findDeviceDialog?.show()
    }
}
