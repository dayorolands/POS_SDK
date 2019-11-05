package com.appzonegroup.creditclub.pos.command

import android.content.Context
import com.telpo.emv.EmvService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
class StartEmvService(val context: Context) : PosCommand() {
    override val tag: String = javaClass.simpleName

    override fun run() {
        var ret = EmvService.Open(context)
        if (ret != EmvService.EMV_TRUE) {
            logError("Emv open failed ! $ret")
            return
        }

        log("Emv open success !")
        ret = EmvService.deviceOpen()
        if (ret != 0) {
            logError("device open failed ! $ret")
            return
        }

        ret = EmvService.NfcOpenReader(1000)
        log("Open NFC : $ret")

        log("device open success !")
    }
}