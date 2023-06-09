package com.cluster.core.util

import android.content.Context
import com.cluster.core.CreditClubException
import com.cluster.core.R
import com.cluster.core.data.api.RequestFailureException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

inline val Throwable?.hasOccurred get() = this !== null

fun Throwable?.isNetworkError() = this != null && (this is IOException || this is HttpException)

fun Throwable?.isServerError() = this != null && this is HttpException

fun Throwable?.isKotlinNPE() = this != null && this is KotlinNullPointerException

fun Throwable?.isTimeout() =
    this != null && this is SocketTimeoutException || this is IOException && (cause.toString()
        .contains("unexpected end of stream") || message
        ?.contains("unexpected end of stream") == true)

fun Throwable?.isInternalServerError() =
    this != null && this is RequestFailureException && this.httpStatusCode == 500

fun Throwable.getMessage(context: Context): String {
    return when {
        this is CreditClubException -> message
        this is RequestFailureException -> message
        this is ConnectException && cause.toString().contains("ECONNREFUSED") -> context.getString(
            R.string.connection_refused_error
        )
        this is ConnectException -> context.getString(R.string.unable_to_connect)
        this is SocketTimeoutException -> context.getString(R.string.request_time_out)
        this is IOException && (cause.toString()
            .contains("unexpected end of stream") || message
            ?.contains("unexpected end of stream") == true) -> context.getString(R.string.unexpected_end_of_stream)
        this is IOException || this is HttpException -> context.getString(R.string.a_network_error_occurred)
        this is SerializationException -> context.getString(R.string.an_internal_error_occurred)
        this is HttpException -> context.getString(R.string.server_response_error)
        this is CancellationException -> context.getString(R.string.cancelled)
        else -> context.getString(R.string.an_internal_error_occurred)
    }
}