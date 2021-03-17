package com.creditclub.core.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

inline class SafeRunResult<out T>(val value: Any?) {

    inline val data: T?
        get() = when {
            isFailure -> null
            else -> value as? T
        }

    val error: Exception?
        get() = when (value) {
            is Failure -> value.exception
            else -> null
        }

    operator fun component1(): T? = data

    operator fun component2(): Exception? = error


    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    inline val isSuccess: Boolean get() = value !is Failure

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    inline val isFailure: Boolean get() = value is Failure

    internal class Failure(
        @JvmField
        val exception: Exception
    ) {
        override fun equals(other: Any?): Boolean = other is Failure && exception == other.exception
        override fun hashCode(): Int = exception.hashCode()
        override fun toString(): String = "Failure($exception)"
    }
}

/**
 * Creates an instance of internal marker [Result.Failure] class to
 * make sure that this class is not exposed in ABI.
 */
@PublishedApi
internal fun createFailure(exception: Exception): Any =
    SafeRunResult.Failure(exception)@PublishedApi

internal fun SafeRunResult<*>.throwOnFailure() {
    if (value is SafeRunResult.Failure) throw value.exception
}

inline fun <T> safeRun(crossinline block: () -> T): SafeRunResult<T> {
    return try {
        SafeRunResult(block())
    } catch (ex: Exception) {
        FirebaseCrashlytics.getInstance().recordException(ex)
        debugOnly { Log.e("safeRun", ex.message, ex) }
        SafeRunResult(createFailure(ex))
    }
}