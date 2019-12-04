package com.appzonegroup.creditclub.pos.provider.mpos

import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.ui.widget.DialogProvider
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice
import com.jhl.jhlblueconn.BlueStateListenerCallback
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class StateListenerCallback(val dialogProvider: DialogProvider) : BlueStateListenerCallback {

    override fun onDeviceInfo(p0: MutableMap<String, String>?) {

    }

    override fun onBluetoothIng() {

    }

    override fun onBluetoothConectedFail() {

    }

    override fun onBluetoothPowerOff() {

    }

    override fun onWaitingForCardSwipe() {
        dialogProvider.showProgressBar("Please swipe your card")
    }

    override fun onTimeout() {

    }

    override fun onScanTimeout() {

    }

    override fun swipCardSucess(p0: String?) {

    }

    override fun onDeviceFound(p0: ArrayList<BluetoothIBridgeDevice>?) {
        dialogProvider.hideProgressBar()

        p0 ?: return

        val options = p0.map { DialogOptionItem(it.deviceName) }
        dialogProvider.showOptions("Select MPos device", options) {
            onSubmit {

            }
        }
    }

    override fun getMacSucess(p0: String?) {

    }

    override fun onBluetoothConected() {

    }

    override fun onDetectIC() {

    }

    override fun onBluetoothDisconnected() {

    }

    override fun onReadCardData(p0: MutableMap<Any?, Any?>?) {

    }

    override fun onLoadMasterKeySucc(p0: Boolean?) {

    }

    override fun onLoadWorkKeySucc(p0: Boolean?) {

    }

    override fun onError(p0: Int, p1: String?) {

    }

    override fun onBluetoothPowerOn() {

    }

    override fun getBatSucess(p0: String?) {

    }
}