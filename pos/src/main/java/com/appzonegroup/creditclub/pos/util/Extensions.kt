package com.appzonegroup.creditclub.pos.util

import android.app.Application
import com.appzonegroup.creditclub.pos.data.PosDatabase


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Application.startPosApp() {
    PosDatabase.getInstance(this)
}