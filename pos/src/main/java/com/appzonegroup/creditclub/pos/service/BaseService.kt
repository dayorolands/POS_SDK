package com.appzonegroup.creditclub.pos.service

import android.content.Intent
import android.os.IBinder
import com.appzonegroup.creditclub.pos.contract.ServiceProvider
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.core.CreditClubService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
abstract class BaseService : CreditClubService(), ServiceProvider {
    override val config by lazy { ConfigService.getInstance(this) }
    override val parameters by lazy { ParameterService.getInstance(this) }
    override val callHomeService by lazy { CallHomeService.getInstance(config, parameters, this) }
    override val isoSocketHelper by lazy { IsoSocketHelper(config, parameters, this) }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}