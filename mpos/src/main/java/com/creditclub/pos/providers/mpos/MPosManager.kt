package com.creditclub.pos.providers.mpos

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.ui.widget.build
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardDataListener
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.CardTransactionStatus
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import com.jhl.jhlblueconn.BlueStateListenerCallback
import com.jhl.jhlblueconn.BluetoothCommmanager
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosManager(activity: CreditClubActivity) : PosManager, BlueStateListenerCallback,
    DialogProvider by activity.dialogProvider, KoinComponent {

    private val parameters: PosParameter by inject()

    private var scanDeviceListenerBlock: DialogListenerBlock<Unit>? = null
    private var readCardBlock: CardDataListener? = null
    private var findDeviceDialog: FindDeviceDialog? = null
//    private var looper = Looper.myLooper() ?: Looper.prepare()

    var device: BluetoothIBridgeDevice? = null
        private set

    override val cardReader: CardReader by lazy { MPosCardReader(this, this) }

    override val sessionData = PosManager.SessionData()

    internal val connectionManager by lazy {
        BluetoothCommmanager.getInstance(this, activity)
    }

    override suspend fun loadEmv() {
    }

    override fun cleanUpEmv() {
        connectionManager.closeResource()
    }

    override fun onDeviceInfo(p0: MutableMap<String, String>?) {
        activity.runOnUiThread {
            hideProgressBar()
            scanDeviceListenerBlock?.build()?.submit(Dialog(context), Unit)
        }
    }

    override fun onUpdateRSAState(bool: Boolean?, i: Int) {
        print("...")
    }

    override fun onBluetoothIng() {
        showProgressBar("Connecting")
    }

    override fun onBluetoothConnectedFail() {
        hideProgressBar()
        showError("Connection failed", scanDeviceListenerBlock)
    }

    override fun onGetCAPublicKeyParamsFailure(i: Int, str: String?) {
        print("...")
    }

    override fun onSwipeCardSuccess(str: String?) {
        hideProgressBar()
        BluetoothCommmanager.inputPassword(parameters.pinKey, 4)
    }

    override fun onLoadMasterKeySuccess(bool: Boolean?) {
    }

    override fun onGetCAPublicKeyListFailure(i: Int, str: String?) {
        print("...")
    }

    override fun onBluetoothPowerOff() {
        print("...")
    }

    override fun onGetCAPublicKeyListSuccess(strArr: Array<out String>?) {
        print("...")
    }

    override fun onWaitingForCardSwipe() {
        showProgressBar("Insert or swipe card", "Please wait", true, scanDeviceListenerBlock)
    }

    override fun onModifyCAPublicKeyFailure(i: Int, str: String?) {
        print("...")
    }

    override fun onAddCAPublicKeyFailure(i: Int, str: String?) {
        print("...")
    }

    override fun onDeleteCAPublicKeyFailure(i: Int, str: String?) {
        print("...")
    }

    override fun onTimeout() {
        hideProgressBar()
        readCardBlock?.invoke(MPosCardData(CardTransactionStatus.Timeout.code, mapOf()))
    }

    override fun onBluetoothConnected() {
        hideProgressBar()
//        connectionManager.writeMainKey(Misc.hexStringToByte(parameters.masterKey))
//        connectionManager.writeWorkKey(Misc.hexStringToByte(parameters.pinKey))
        connectionManager.deviceInfo
    }

    override fun onGetCAPublicKeyParamsSuccess(str: String?) {
        print("...")
    }

    override fun onDeviceFound(devices: ArrayList<BluetoothIBridgeDevice>?) {

        if (devices.isNullOrEmpty()) return showError("No devices found", scanDeviceListenerBlock)

        synchronized(devices as Any) {
            val it = devices.iterator()
            val deviceList = mutableListOf<BluetoothIBridgeDevice>()

            while (it.hasNext()) {
                val device = it.next()

                deviceList.add(device)
            }

            activity.runOnUiThread {
                if (deviceList.size != 0) {
                    findDeviceDialog?.updateList(devices)
                }
            }
        }
    }

    override fun onGetMacSuccess(str: String?) {
        print("...")
    }

    override fun onGetBatterySuccess(bool: Boolean?, str: String?) {
        print("...")
    }

    override fun onDetectIC() {
        showProgressBar("Please enter amount", "IC card detected", true, scanDeviceListenerBlock)
    }

    override fun onDeleteCAPublicKeySuccess() {
        print("...")
    }

    override fun onLoadWorkKeySuccess(bool: Boolean?) {
    }

    override fun onAddCAPublicKeySuccess() {
        print("...")
    }

    override fun onBluetoothDisconnected() {
        showError("Device disconnected", scanDeviceListenerBlock)
    }

    override fun onGoOnlineProcess(bool: Boolean?, i: Int, str: String?) {
        print("...")
    }

    override fun onReadCardData(p0: MutableMap<Any?, Any?>?) {
        activity.runOnUiThread {
            hideProgressBar()
            readCardBlock?.invoke(MPosCardData(1, p0 as Map<String, String>))
        }
    }

    override fun onModifyCAPublicKeySuccess() {
        print("...")
    }

    override fun onError(p0: Int, p1: String?) {
        activity.runOnUiThread {
            hideProgressBar()
            readCardBlock?.invoke(MPosCardData(p0, mapOf()))
        }
    }

    override fun onBluetoothPowerOn() {
        print("...")
    }

    internal fun findDevice(block: DialogListenerBlock<Unit>) {
        scanDeviceListenerBlock = block
        findDeviceDialog = FindDeviceDialog(context) {
            onSubmit { device ->
                connectionManager.connectDevice(device.deviceAddress)
                findDeviceDialog?.dismiss()
                findDeviceDialog = null
            }

            onClose {
                connectionManager.StopScanDevice()
                scanDeviceListenerBlock?.build()?.close()
            }
        }
        findDeviceDialog?.show()

        connectionManager.scanDevice(null, 0)
    }

    internal fun readCard(block: CardDataListener) {
        readCardBlock = block
        findDevice {
            onSubmit {
                connectionManager.swipeCard(4000, 0)
            }

            onClose {
                block(MPosCardData(-1, mapOf()))
            }
        }
    }

    companion object : PosManagerCompanion {
        override val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                MPosManager(activity)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            return try {
                BluetoothAdapter.getDefaultAdapter() != null
            } catch (ex: Exception) {
                false
            }
        }

        override fun setup(context: Context) {}
    }
}