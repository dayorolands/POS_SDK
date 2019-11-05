package com.creditclub.core.util

import com.creditclub.core.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */

suspend fun <T> ioContext(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO) { block() }

suspend fun <T> safeRunIO(block: suspend CoroutineScope.() -> T): SafeRunResult<T> = withContext(Dispatchers.IO) {
    var data: T? = null
    var error: Exception? = null

    try {
        data = block()
    } catch (ex: Exception) {
        error = ex
        if (BuildConfig.DEBUG) ex.printStackTrace()
    }

    SafeRunResult(data, error)
}