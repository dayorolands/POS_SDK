package com.appzonegroup.creditclub.pos.util.delegate

import org.jpos.iso.ISOMsg
import kotlin.reflect.KProperty


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */
class IsoMsgFieldDelegate(private val fldno: Int) {

    operator fun getValue(isoMsg: ISOMsg, property: KProperty<*>): String? {
        return isoMsg.getString(fldno)
    }

    operator fun setValue(isoMsg: ISOMsg, property: KProperty<*>, value: String?) {
        isoMsg.set(fldno, value)
    }

}

fun isoMsgField(fldno: Int) = IsoMsgFieldDelegate(fldno)