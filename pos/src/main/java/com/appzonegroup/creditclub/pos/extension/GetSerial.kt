package com.appzonegroup.creditclub.pos.extension

import android.os.Build
import java.lang.reflect.Method


val posSerialNumber: String?
    get() {
        var serialNumber: Any?

        try {
            val c = Class.forName("android.os.SystemProperties")
            val get: Method = c.getMethod("get", String::class.java)

            serialNumber = get.invoke(c, "gsm.sn1")
            if (serialNumber == "") serialNumber = get.invoke(c, "ril.serialnumber")
            if (serialNumber == "") serialNumber = get.invoke(c, "ro.serialno")
            if (serialNumber == "") serialNumber = get.invoke(c, "sys.serialnumber")
            if (serialNumber == "") serialNumber = Build.SERIAL

            if (serialNumber == "") serialNumber = null
        } catch (e: Exception) {
            e.printStackTrace()
            serialNumber = null
        }

        return serialNumber as String?
    }