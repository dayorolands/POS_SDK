package com.cluster.core.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */

typealias SuspendCallback = suspend () -> Unit

suspend inline fun <T> safeRunIO(crossinline block: suspend () -> T): SafeRunResult<T> =
    withContext(Dispatchers.IO) {
        try {
            SafeRunResult(block())
        } catch (ex: Exception) {
            debugOnly { Log.e("safeRunIO", ex.message, ex) }
            SafeRunResult(createFailure(ex))
        }
    }

suspend inline fun <T> safeRunSuspend(crossinline block: suspend () -> T): SafeRunResult<T> {
    return try {
        SafeRunResult(block())
    } catch (ex: Exception) {
        debugOnly { Log.e("safeRun", ex.message, ex) }
        SafeRunResult(createFailure(ex))
    }
}