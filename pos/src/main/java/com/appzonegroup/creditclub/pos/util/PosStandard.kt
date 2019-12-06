package com.appzonegroup.creditclub.pos.util

import android.content.Context
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 18/10/2019.
 * Appzone Ltd
 */

inline val Context.posConfig get() = ConfigService.getInstance(this)
inline val Context.posParameters get() = ParameterService.getInstance(this)