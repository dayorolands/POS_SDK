package com.appzonegroup.creditclub.pos.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appzonegroup.creditclub.pos.contract.ServiceProvider
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
abstract class BaseService : Service(), ServiceProvider {
    override val config by lazy { ConfigService.getInstance(this) }
    override val parameters by lazy { ParameterService.getInstance(this) }
    override val callHomeService by lazy { CallHomeService.getInstance(config, parameters) }
    override val isoSocketHelper by lazy { IsoSocketHelper(config, parameters) }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}