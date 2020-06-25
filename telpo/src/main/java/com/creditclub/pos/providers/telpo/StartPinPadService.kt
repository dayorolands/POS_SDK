package com.creditclub.pos.providers.telpo

import android.content.Context
import com.creditclub.pos.PosParameter
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
        val i = PinpadService.TP_WriteMasterKey(0, pinKey, PinpadService.KEY_WRITE_DIRECT)

        if (i == 0) {
            val masterKey = parameters.masterKey.hexBytes
            PinpadService.TP_WritePinKey(1, masterKey, PinpadService.KEY_WRITE_DECRYPT, 0)
        }
    }

    private inline val String.hexBytes: ByteArray? get() {
        if (isBlank()) return null
        val len = length / 2
        val result = ByteArray(len)
        val achar = toUpperCase(Locale.getDefault()).toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = (achar[pos].index shl 4 or achar[pos + 1].index).toByte()
        }
        return result
    }

    private inline val Char.index: Int get() = "0123456789ABCDEF".indexOf(this)
}