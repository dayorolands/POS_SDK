package com.appzonegroup.creditclub.pos.provider.qpos

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.view.WindowManager
import com.appzonegroup.creditclub.pos.card.CardDataListener
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.CardTransactionStatus
import com.appzonegroup.creditclub.pos.card.PosManager
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.ui.widget.build
import com.dspread.xpos.QPOSService
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import com.jhl.jhlblueconn.BluetoothCommmanager
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class QPosManager(activity: CreditClubActivity) : PosManager, BlueStateListenerCallback,
    DialogProvider by activity.dialogProvider, KoinComponent {

    private val parameters: ParameterService by inject()

    private var scanDeviceListenerBlock: DialogListenerBlock<Unit>? = null
    private var readCardBlock: CardDataListener? = null
    private var findDeviceDialog: FindDeviceDialog? = null
    private val keyController by lazy { KeyController() }
    internal val pos by lazy { QPOSService.getInstance(QPOSService.CommunicationMode.BLUETOOTH); }
    private val listener by lazy { QPosListener(this) }

    var device: BluetoothIBridgeDevice? = null
        private set

    override val cardReader: CardReader by lazy { QPosCardReader(this, this) }
    override val sessionData = PosManager.SessionData()

    private val activeJobs = mutableListOf<Job?>()

    override fun loadEmv() {
        pos.setContext(activity)
        pos.initListener(listener)
        pos.clearBluetoothBuffer()
//        pos.setTerminalID()
        pos.scanQPos2Mode(activity, 20)
    }

    override fun cleanUpEmv() {
        pos.stopScanQPos2Mode()
        listener.cleanup()
        if (pos == null) return
        pos.disconnectBT()
    }

    override fun onDeviceInfo(p0: MutableMap<String, String>?) {
        activity.runOnUiThread {
            hideProgressBar()
            scanDeviceListenerBlock?.build()?.submit(Dialog(context), Unit)
        }
    }

    override fun onUpdateRSAState(bool: Boolean?, i: Int) {
        print("...")
//        connectionManager.deviceInfo
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
//        connectionManager.writeWorkKey(Misc.hexStringToByte(parameters.pinKey))
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
                MPosCardData(CardTransactionStatus.Timeout.code, mapOf(), null)
            )
        }
    }

    override fun onBluetoothConnected() {
        hideProgressBar()
//        connectionManager.writeMainKey(Misc.hexStringToByte(parameters.masterKey))
    }

    override fun onGetCAPublicKeyParamsSuccess(str: String?) {
        print("...")
    }

    override fun onDeviceFound() {
        val devices = pos.deviceList.map { it }
        if (devices.isNullOrEmpty()) return showError("No devices found", scanDeviceListenerBlock)

        synchronized(devices as Any) {
            activity.runOnUiThread {
                if (devices.isNotEmpty()) {
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
        showProgressBar("Securing connection")

        val publicKey = keyController.publicKeyHex

        publicKey?.also {
            //            connectionManager.setTimeout(10000)
//            connectionManager.updateRSA(it)
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

    @Suppress("UNCHECKED_CAST")
    override fun onReadCardData(p0: MutableMap<Any?, Any?>?) {
        activity.runOnUiThread {
            val job = GlobalScope.launch(Dispatchers.Main) {
                showProgressBar("Processing")

                val cardData = withContext(Dispatchers.Default) {
                    MPosCardData(1, p0 as Map<String, String>, keyController)
                }

                hideProgressBar()
                readCardBlock?.invoke(cardData)
            }
            registerJob(job)
        }
    }

    private fun registerJob(job: Job) {
        activeJobs.add(job)
        job.invokeOnCompletion {
            activeJobs.remove(job)
            hideProgressBar()
        }
    }

    override fun onModifyCAPublicKeySuccess() {
        print("...")
    }

    override fun onError(p0: Int, p1: String?) {
        activeJobs.forEach { it?.cancel() }
        activity.runOnUiThread {
            hideProgressBar()
            readCardBlock?.invoke(MPosCardData(MPosErrorCode[p0], mapOf(), null))
        }
    }

    override fun onBluetoothPowerOn() {
        print("...")
    }

    internal fun findDevice(block: DialogListenerBlock<Unit>) {
        scanDeviceListenerBlock = block
        findDeviceDialog = FindDeviceDialog(context) {
            onSubmit { device ->
                pos.stopScanQPos2Mode()
                pos.connectBluetoothDevice(true, 25, device.address)
                findDeviceDialog?.dismiss()
                findDeviceDialog = null
            }

            onClose {
                pos.stopScanQPos2Mode()
                scanDeviceListenerBlock?.build()?.close()
            }
        }
        findDeviceDialog?.show()

        pos.stopScanQPos2Mode()
    }

    internal fun readCard(block: CardDataListener) {
        readCardBlock = block
        findDevice {
            onSubmit {
                //                pos.doCheckCard()
            }

            onClose {
                block(MPosCardData(-1, mapOf(), null))
            }
        }
    }

    init {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    companion object {
        val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                QPosManager(activity)
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