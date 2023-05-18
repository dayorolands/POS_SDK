package com.dspread.qpos

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cluster.core.config.getBluetoothMessage
import com.cluster.core.config.requestBluetoothPermissions
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.format
import com.cluster.pos.card.*
import com.dspread.qpos.utils.FileUtils
import com.dspread.xpos.QPOSService
import com.eazypermissions.common.model.PermissionResult
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import java.time.Instant
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class QPosCardReader(
    private val activity: CreditClubActivity,
    private val pos: QPOSService,
    private val listener: QPosListener,
    private val mainScope: CoroutineScope
) : CardReaders, KoinComponent {

    private val keyIndex = 0
    private var watchJob: Job? = null
    private val dialogProvider = activity.dialogProvider
    private var isConfigUpdated: Boolean = false

    override suspend fun waitForCard(): CardReaderEvent {
        val bluetoothPermission = requestBluetoothPermissions(activity = activity)
        Log.d("CheckPermission", "Here are the bluetooth permissions ${bluetoothPermission.requestCode}")
        if (bluetoothPermission !is PermissionResult.PermissionGranted) {
            dialogProvider.showErrorAndWait(bluetoothPermission.getBluetoothMessage())
            return CardReaderEvent.CANCELLED
        }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled && !turnOnBluetooth()) {
            dialogProvider.showErrorAndWait("Could not turn on bluetooth")
            return CardReaderEvent.CANCELLED
        }

        Looper.myLooper() ?: Looper.prepare()
        val handler = Handler(Looper.myLooper()!!)
        pos.initListener(handler, listener)
        pos.clearBluetoothBuffer()
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

    override suspend fun read(amountStr: String): CardData? {
        dialogProvider.showProgressBar("Securing Connection")
        if(!isConfigUpdated) {
            Log.d("CheckConfig", "Here to check if the config is updated")
            updateEmvConfig()
        }
        dialogProvider.hideProgressBar()
        val terminalTime = Instant.now().format("HHmmss")
        val cardNo = listener.waitForString { pos.getIccCardNo(terminalTime) } ?: return null
        Log.d("onRequest", "Check the card no returned here $cardNo")
        if (cardNo.isBlank()) {
            return QposCardData().apply {
                status = CardTransactionStatus.Failure
            }
        }
        loadKeys()
        return suspendCoroutine { continuation ->
            listener.cardDataContinuation = continuation
            pos.doTrade(keyIndex, 120)
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
        val emvProfileTlv = String(FileUtils.readAssetsLine("emv_profile_tlv_cr100.xml", activity))
        listener.waitForUnit {
            pos.updateEMVConfigByXml(emvProfileTlv)
        }
        isConfigUpdated = true
    }

    private suspend fun loadKeys() {
        val ksn = "09118012400705E00000"
        val ipekString = "C22766F7379DD38AA5E1DA8C6AFA75AC"
        val kcvString = "B2DE27F60A443944"

        listener.waitForUnit {
            pos.updateIPEKOperationByKeyType(
                "01",
                ksn,
                ipekString,
                kcvString,
                ksn,
                ipekString,
                kcvString,
                ksn,
                ipekString,
                kcvString
            )
        }
    }

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
