package com.appzonegroup.creditclub.pos.provider.mpos

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import com.appzonegroup.creditclub.pos.card.CardDataListener
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.CardTransactionStatus
import com.appzonegroup.creditclub.pos.card.PosManager
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.Misc
import com.appzonegroup.creditclub.pos.util.TerminalUtils
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.ui.widget.build
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import com.jhl.jhlblueconn.BlueStateListenerCallback
import com.jhl.jhlblueconn.BluetoothCommmanager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val parameters: ParameterService by inject()

    private var scanDeviceListenerBlock: DialogListenerBlock<Unit>? = null
    private var readCardBlock: CardDataListener? = null
    private var findDeviceDialog: FindDeviceDialog? = null
    //    private var looper = Looper.myLooper() ?: Looper.prepare()
    private val keyController by lazy { KeyController() }

    var device: BluetoothIBridgeDevice? = null
        private set

    override val cardReader: CardReader by lazy { MPosCardReader(this, this) }

    override val sessionData = PosManager.SessionData()

    internal val connectionManager by lazy {
        BluetoothCommmanager.getInstance(this, activity)
    }

    override fun loadEmv() {
        println("...")
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
        connectionManager.deviceInfo
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
        print("...")
        connectionManager.writeWorkKey(Misc.hexStringToByte(parameters.masterKey))
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
        activity.runOnUiThread {
            hideProgressBar()
            readCardBlock?.invoke(
                MPosCardData(CardTransactionStatus.Timeout.code, mapOf(), keyController)
            )
        }
    }

    override fun onBluetoothConnected() {
        hideProgressBar()
        connectionManager.writeMainKey(Misc.hexStringToByte(parameters.pinKey))
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
        print("...")
        val publicKey = keyController.generatePrivateAndPublicKeys()
        publicKey?.also {
            connectionManager.setTimeout(10000)
            connectionManager.updateRSA(it)
        }
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
            GlobalScope.launch(Dispatchers.Main) {
                showProgressBar("Processing")

                val track2 = withContext(Dispatchers.Default) {
                    keyController.decrypt(TerminalUtils.hexStringToByteArray(p0!!["EncryptTrack2"] as String))
                }

                hideProgressBar()
                readCardBlock?.invoke(MPosCardData(1, p0 as Map<String, String>, keyController))
            }
        }
    }

    override fun onModifyCAPublicKeySuccess() {
        print("...")
    }

    override fun onError(p0: Int, p1: String?) {
        activity.runOnUiThread {
            hideProgressBar()
            readCardBlock?.invoke(MPosCardData(MPosErrorCode[p0], mapOf(), keyController))
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
                block(MPosCardData(-1, mapOf(), keyController))
            }
        }
    }

    companion object {
        val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                MPosManager(activity)
            }
        }

        fun isCompatible(): Boolean {
            return try {
                BluetoothAdapter.getDefaultAdapter() != null
            } catch (ex: Exception) {
                false
            }
        }
    }
}