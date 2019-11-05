package com.creditclub.core.util

import android.app.Dialog
import com.creditclub.core.R
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogProvider


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

/***
 * Show this [Dialog] when [DialogListener.onClose] is called
 */
inline val Dialog.showOnClose: DialogListenerBlock<Nothing>
    get() = {
        onClose {
            show()
        }
    }

fun DialogProvider.showNetworkError() = showNetworkError<Nothing>(null)

fun <T> DialogProvider.showNetworkError(block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = context.getString(R.string.a_network_error_occurred)

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showInternalError() = showNetworkError<Nothing>(null)

fun <T> DialogProvider.showInternalError(block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = "An internal error occurred. Please try again later"

    block ?: return showError(message)
    showError(message, block)
}

fun <T> DialogProvider.showError(exception: Exception, block: DialogListenerBlock<T>?) {
    hideProgressBar()
    val message = exception.getMessage(context)

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showError(exception: Exception) = showError<Nothing>(exception, null)