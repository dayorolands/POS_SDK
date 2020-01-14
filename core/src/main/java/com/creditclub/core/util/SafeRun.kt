package com.creditclub.core.util

import com.crashlytics.android.Crashlytics
import com.creditclub.core.BuildConfig


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */

data class SafeRunResult<T>(val data: T?, val error: Exception?)

inline fun <T> safeRun(crossinline block: () -> T): SafeRunResult<T> {
    var data: T? = null
    var error: Exception? = null

    try {
        data = block()
    } catch (ex: Exception) {
        error = ex
        try {
            Crashlytics.logException(ex)
        } catch (ex: Exception) {
            if (BuildConfig.DEBUG) ex.printStackTrace()
        }
        if (BuildConfig.DEBUG) ex.printStackTrace()
    }

    return SafeRunResult(data, error)
}