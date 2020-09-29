package com.creditclub.pos.providers.telpo

import android.content.Context
import com.creditclub.pos.PosParameter
import com.creditclub.pos.extensions.hexBytes
import com.telpo.pinpad.PinpadService
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */

class StartPinPadService(private val context: Context, private val parameters: PosParameter) :
    Runnable {

    override fun run() {
        val ret = PinpadService.Open(context)
        if (ret == PinpadService.PIN_ERROR_NEED_TO_FOMRAT) {
            PinpadService.TP_PinpadFormat(context)
            PinpadService.Open(context)
        }

        val pinKey = parameters.pinKey.hexBytes
        PinpadService.TP_WriteMasterKey(0, pinKey, PinpadService.KEY_WRITE_DIRECT)

        val masterKey = parameters.masterKey.hexBytes
        PinpadService.TP_WritePinKey(1, masterKey, PinpadService.KEY_WRITE_DECRYPT, 0)
    }
}