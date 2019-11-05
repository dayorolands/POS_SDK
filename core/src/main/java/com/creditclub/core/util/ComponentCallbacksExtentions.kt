package com.creditclub.core.util

import android.content.ComponentCallbacks
import com.creditclub.core.data.CreditClubMiddleWareAPI
import org.koin.android.ext.android.get


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */

val ComponentCallbacks.creditClubMiddleWareAPI: CreditClubMiddleWareAPI get() = get()