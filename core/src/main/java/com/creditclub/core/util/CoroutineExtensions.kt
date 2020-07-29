package com.creditclub.core.util

import android.util.Log
import com.creditclub.core.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            FirebaseCrashlytics.getInstance().recordException(ex)
            debugOnly {
                Log.e("safeRunIO", ex.message, ex)
                ex.printStackTrace()
            }
        }

        SafeRunResult(data, error)
    }

suspend inline fun <T> safeRunSuspend(crossinline block: suspend () -> T): SafeRunResult<T> {
    var data: T? = null
    var error: Exception? = null

    try {
        data = block()
    } catch (ex: Exception) {
        error = ex
        FirebaseCrashlytics.getInstance().recordException(ex)
        debugOnly {
            Log.e("safeRun", ex.message, ex)
            ex.printStackTrace()
        }
    }

    return SafeRunResult(data, error)
}