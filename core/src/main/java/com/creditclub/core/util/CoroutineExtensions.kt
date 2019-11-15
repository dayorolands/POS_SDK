package com.creditclub.core.util

import com.crashlytics.android.Crashlytics
import com.creditclub.core.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */

suspend inline fun <T> ioContext(crossinline block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO) { block() }

suspend inline fun <T> safeRunIO(crossinline block: suspend CoroutineScope.() -> T): SafeRunResult<T> =
    withContext(Dispatchers.IO) {
        var data: T? = null
        var error: Exception? = null

        try {
            data = block()
        } catch (ex: Exception) {
            error = ex
            Crashlytics.logException(ex)
            if (BuildConfig.DEBUG) ex.printStackTrace()
        }

        SafeRunResult(data, error)
    }