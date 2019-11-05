package com.appzonegroup.creditclub.pos.contract

import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */

interface ServiceProvider {
    val config: ConfigService
    val parameters: ParameterService
    val callHomeService: CallHomeService
    val isoSocketHelper: IsoSocketHelper
}