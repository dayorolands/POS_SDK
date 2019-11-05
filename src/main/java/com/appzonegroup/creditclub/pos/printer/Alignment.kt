package com.appzonegroup.creditclub.pos.printer

import com.telpo.tps550.api.printer.UsbThermalPrinter


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
enum class Alignment(val code: Int) {
    LEFT(UsbThermalPrinter.ALGIN_LEFT),
    MIDDLE(UsbThermalPrinter.ALGIN_MIDDLE),
    RIGHT(UsbThermalPrinter.ALGIN_RIGHT)
}