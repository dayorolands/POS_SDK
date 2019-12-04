package com.appzonegroup.creditclub.pos.provider.telpo

import android.content.Context
import com.appzonegroup.creditclub.pos.command.PosCommand
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.Misc
import com.telpo.pinpad.PinpadService

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */

class StartPinPadService(private val context: Context, private val parameters: ParameterService) : PosCommand() {
    override val tag: String = javaClass.simpleName

    override fun run() {
        var ret = PinpadService.Open(context)
        log("deviceOpen open:$ret")

        if (ret == PinpadService.PIN_ERROR_NEED_TO_FOMRAT) {
            PinpadService.TP_PinpadFormat(context)
            ret = PinpadService.Open(context)
        }

        log("deviceOpen open:$ret")
//        log("Master Key: ${parameters.masterKey}")
//        log("PIN Key: ${parameters.pinKey}")

//        val masterKey = Misc.hexStringToByte(parameters.masterKey)
        val pinKey = Misc.hexStringToByte(parameters.pinKey)
        val i = PinpadService.TP_WriteMasterKey(0, pinKey, PinpadService.KEY_WRITE_DIRECT)

        log("TP_WriteMasterKey:$i")

        if (i == 0) {
            val masterKey = Misc.hexStringToByte(parameters.masterKey)
//            val pinKey = Misc.hexStringToByte(parameters.pinKey)
            val t = PinpadService.TP_WritePinKey(1, masterKey, PinpadService.KEY_WRITE_DECRYPT, 0)
            log("TP_WritePinKey:$t")
        }
    }
}