package com.creditclub.core.util

import com.creditclub.core.R
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 30/09/2019.
 * Appzone Ltd
 */

fun DialogProvider.showNetworkError() = showNetworkError<Nothing>(null)

fun <T> DialogProvider.showNetworkError(block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = activity.getString(R.string.a_network_error_occurred)

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showInternalError() = showNetworkError<Nothing>(null)

fun <T> DialogProvider.showInternalError(block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = activity.getString(R.string.an_internal_error_occurred)

    block ?: return showError(message)
    showError(message, block)
}

fun <T> DialogProvider.showError(exception: Exception, block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = exception.getMessage(activity)


    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showError(exception: Exception) = showError<Nothing>(exception, null)