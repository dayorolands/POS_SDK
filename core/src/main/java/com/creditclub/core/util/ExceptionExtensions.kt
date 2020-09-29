package com.creditclub.core.util

import android.content.Context
import com.creditclub.core.R
import com.creditclub.core.data.api.RequestFailureException
import kotlinx.serialization.json.JsonDecodingException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

inline val Exception?.hasOccurred get() = this !== null

fun Exception?.isNetworkError() = this != null && (this is IOException || this is HttpException)

fun Exception?.isServerError() = this != null && this is HttpException

fun Exception?.isKotlinNPE() = this != null && this is KotlinNullPointerException

fun Exception.getMessage(context: Context): String {
    return when {
        this is RequestFailureException -> message
        this is ConnectException && cause.toString().contains("ECONNREFUSED") -> context.getString(
            R.string.connection_refused_error
        )
        this is SocketTimeoutException -> context.getString(R.string.request_time_out)
        this is IOException || this is HttpException -> context.getString(R.string.a_network_error_occurred)
        this is JsonDecodingException -> context.getString(R.string.an_internal_error_occurred)
        this is HttpException -> context.getString(R.string.server_response_error)
        else -> context.getString(R.string.an_internal_error_occurred)
    }
}