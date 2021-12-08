package com.cluster.pos.extension

import android.annotation.SuppressLint
import android.os.Build
import java.lang.reflect.Method


var serialNumber: Any? = null

@SuppressLint("PrivateApi", "BanUncheckedReflection")
fun getPosSerialNumber(): String? {
    if (serialNumber != null) return serialNumber as String?

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