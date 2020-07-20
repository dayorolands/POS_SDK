package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import com.appzonegroup.creditclub.pos.util.AppConstants
import com.appzonegroup.creditclub.pos.util.PosMode
import com.creditclub.core.util.delegates.valueStore
import com.creditclub.pos.PosConfig
import com.creditclub.pos.RemoteConnectionInfo


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
open class ConfigService protected constructor(
    context: Context,
    private val prefs: SharedPreferences
) : PosConfig, SharedPreferences by prefs {

    override var apn = prefs.getString("APN", AppConstants.APN) as String
        set(value) {
            field = value
            prefs.edit().putString("APN", value).apply()
        }

    override var host = prefs.getString("HOST", AppConstants.HOST) as String
        set(value) {
            field = value
            prefs.edit().putString("HOST", value).apply()
        }

    override var ip = prefs.getString("IP", AppConstants.IP) as String
        set(value) {
            field = value
            prefs.edit().putString("IP", value).apply()
        }

    override var port = prefs.getInt("PORT", AppConstants.PORT)
        set(value) {
            field = value
            prefs.edit().putInt("PORT", value).apply()
        }

    override var callHome = prefs.getString("CALL_HOME", AppConstants.CALL_HOME) as String
        set(value) {
            field = value
            prefs.edit().putString("CALL_HOME", value).apply()
        }

    override var terminalId = "2076DK33"

    override var supervisorPin = prefs.getString("SUPERVISOR_PIN", "1111") as String
        set(value) {
            field = value
            prefs.edit().putString("SUPERVISOR_PIN", value).apply()
        }

    override var adminPin = prefs.getString("ADMIN_PIN", "asdfg") as String
        set(value) {
            field = value
            prefs.edit().putString("ADMIN_PIN", value).apply()
        }

    override var remoteConnectionInfo: RemoteConnectionInfo
        get() = posMode
        set(value) {
            posModeStr = value.id
        }

    var posModeStr: String? by valueStore("POS_MODE", "EPMS")

    var posMode: PosMode
        get() {
            return when (posModeStr) {
                "EPMS" -> PosMode.EPMS
                "EPMS_TEST" -> PosMode.EPMS_TEST
                "POSVAS_TEST" -> PosMode.POSVAS_TEST
                else -> PosMode.POSVAS
            }
        }
        set(value) {
            posModeStr = value.id
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