package com.cluster.pos.service

import android.content.Context
import android.content.SharedPreferences
import com.cluster.pos.util.AppConstants
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.util.delegates.intStore
import com.cluster.core.util.delegates.valueStore
import com.cluster.pos.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.component.get
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/27/2019.
 * Appzone Ltd
 */
open class ConfigService(context: Context) : PosConfig, KoinComponent {
    private val prefs: SharedPreferences = context.getEncryptedSharedPreferences(DEFAULT_FILE_NAME)
    override var apn by prefs.valueStore("APN", AppConstants.APN)
    override var host by prefs.valueStore("HOST", AppConstants.HOST)
    override var ip by prefs.valueStore("IP", AppConstants.IP)
    override var port by prefs.intStore("PORT", AppConstants.PORT)

    override var callHome by prefs.valueStore("CALL_HOME", AppConstants.CALL_HOME)
    override var terminalId by prefs.valueStore("TERMINAL_ID", "2076DT44") //2076EU44
    override var supervisorPin by prefs.valueStore("SUPERVISOR_PIN", "1111")
    override var adminPin by prefs.valueStore("ADMIN_PIN", "ordaafrica")

    override var remoteConnectionInfo: RemoteConnectionInfo
        get() {
            if (terminalId.uppercase().startsWith("2APP")) return InvalidRemoteConnectionInfo
            return get()
        }
        set(value) {
            loadKoinModules(module {
                single { value }
                single<PosParameter> {
                    remoteConnectionInfo.getParameter(androidContext())
                }
            })
        }

    companion object {
        const val DEFAULT_FILE_NAME = "pos_config_0"
    }
}