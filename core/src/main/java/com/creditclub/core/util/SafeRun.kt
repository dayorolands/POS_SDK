package com.creditclub.core.util


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */

data class SafeRunResult<T>(val data: T?, val error: Exception?)

fun <T> safeRun(block: () -> T): SafeRunResult<T> {
    var data: T? = null
    var error: Exception? = null

    try {
        data = block()
    } catch (ex: Exception) {
        error = ex
        ex.printStackTrace()
    }

    return SafeRunResult(data, error)
}