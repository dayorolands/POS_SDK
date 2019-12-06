package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.appzonegroup.creditclub.pos.util.AppConstants
import com.appzonegroup.creditclub.pos.util.PosMode
import com.creditclub.core.util.delegates.valueStore


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
open class ConfigService protected constructor(
    context: Context,
    private val prefs: SharedPreferences
) : SharedPreferences by prefs {

    open var apn = prefs.getString("APN", AppConstants.APN) as String
        set(value) {
            field = value
            prefs.edit().putString("APN", value).apply()
        }

    open var host = prefs.getString("HOST", AppConstants.HOST) as String
        set(value) {
            field = value
            prefs.edit().putString("HOST", value).apply()
        }

    open var ip = prefs.getString("IP", AppConstants.IP) as String
        set(value) {
            field = value
            prefs.edit().putString("IP", value).apply()
        }

    open var port = prefs.getInt("PORT", AppConstants.PORT)
        set(value) {
            field = value
            prefs.edit().putInt("PORT", value).apply()
        }

    open var callHome = prefs.getString("CALL_HOME", AppConstants.CALL_HOME) as String
        set(value) {
            field = value
            prefs.edit().putString("CALL_HOME", value).apply()
        }

    open var terminalId = "2076DK33"

    open var supervisorPin = prefs.getString("SUPERVISOR_PIN", "1111") as String
        set(value) {
            field = value
            prefs.edit().putString("SUPERVISOR_PIN", value).apply()
        }

    open var adminPin = prefs.getString("ADMIN_PIN", "asdfg") as String
        set(value) {
            field = value
            prefs.edit().putString("ADMIN_PIN", value).apply()
        }

    open var posModeStr: String? by valueStore("POS_MODE", "POSVAS")

    open var posMode: PosMode
        get() {
            return when (posModeStr) {
                "EPMS" -> PosMode.EPMS
                "EPMS_TEST" -> PosMode.EPMS_TEST
                "POSVAS_TEST" -> PosMode.POSVAS_TEST
                else -> PosMode.POSVAS
            }
        }
        set(value) {
            posModeStr = value.name
        }

    fun resetNetwork() {
        apn = AppConstants.APN
        host = AppConstants.HOST
        terminalId = AppConstants.TID
        port = AppConstants.PORT
        ip = AppConstants.IP
        callHome = AppConstants.CALL_HOME
    }

    fun getApnInfo(context: Context): String {
        val mag = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val mobInfo = mag.activeNetworkInfo
            return mobInfo?.extraInfo ?: "No active network. Turn on mobile data"
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return "No active network. Turn on mobile data"
    }

    companion object {
        private var INSTANCE: ConfigService? = null

        fun getInstance(context: Context): ConfigService {
            if (INSTANCE == null) INSTANCE = ConfigService(
                context.applicationContext,
                context.getSharedPreferences("Config", 0)
            )
            return INSTANCE as ConfigService
        }
    }
}