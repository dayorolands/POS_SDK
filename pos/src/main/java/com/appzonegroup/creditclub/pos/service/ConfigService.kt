package com.appzonegroup.creditclub.pos.service

import android.content.Context
import android.content.SharedPreferences
import com.appzonegroup.creditclub.pos.util.AppConstants
import com.appzonegroup.creditclub.pos.util.PosMode
import com.creditclub.core.util.delegates.booleanStore
import com.creditclub.core.util.delegates.intStore
import com.creditclub.core.util.delegates.stringStore
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.utils.nonNullStringStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
open class ConfigService(context: Context) : PosConfig {
    private val prefs: SharedPreferences = context.getSharedPreferences("Config", 0)
    override var apn by prefs.nonNullStringStore("APN", AppConstants.APN)
    override var host by prefs.nonNullStringStore("HOST", AppConstants.HOST)
    override var ip by prefs.nonNullStringStore("IP", AppConstants.IP)
    override var port by prefs.intStore("PORT", AppConstants.PORT)

    override var callHome by prefs.nonNullStringStore("CALL_HOME", AppConstants.CALL_HOME)
    override var terminalId by prefs.nonNullStringStore("TERMINAL_ID", "")
    override var supervisorPin by prefs.nonNullStringStore("SUPERVISOR_PIN", "1111")
    override var adminPin by prefs.nonNullStringStore("ADMIN_PIN", "asdfg")

    override var remoteConnectionInfo: RemoteConnectionInfo
        get() = PosMode.values().find { it.id == posModeStr } ?: PosMode.EPMS
        set(value) {
            posModeStr = value.id
            loadKoinModules(module {
                single<PosParameter>(override = true) {
                    ParameterService(androidContext(), value)
                }
            })
        }

    private var posModeStr: String? by prefs.stringStore("POS_MODE", "EPMS")
}