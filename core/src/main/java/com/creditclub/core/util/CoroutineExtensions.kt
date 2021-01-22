package com.creditclub.core.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */

suspend inline fun <T> safeRunIO(crossinline block: suspend CoroutineScope.() -> T): SafeRunResult<T> =
    withContext(Dispatchers.IO) {
        try {
            SafeRunResult(block())
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            debugOnly { Log.e("safeRunIO", ex.message, ex) }
            SafeRunResult(SafeRunResult.Failure(ex))
        }
    }

suspend inline fun <T> safeRunSuspend(crossinline block: suspend () -> T): SafeRunResult<T> {
    return try {
        SafeRunResult(block())
    } catch (ex: Exception) {
        FirebaseCrashlytics.getInstance().recordException(ex)
        debugOnly { Log.e("safeRun", ex.message, ex) }
        SafeRunResult(SafeRunResult.Failure(ex))
    }
}