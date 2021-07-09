package com.creditclub.core.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.creditclub.core.R
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

fun DialogProvider.showNetworkError() = showNetworkError(null)

fun DialogProvider.showNetworkError(block: DialogListenerBlock<*>?) {
    hideProgressBar()
    val message = context.getString(R.string.a_network_error_occurred)

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showInternalError() = showNetworkError(null)

fun DialogProvider.showInternalError(block: DialogListenerBlock<*>?) {
    hideProgressBar()
    val message = "An internal error occurred. Please try again later"

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showError(exception: Exception, block: DialogListenerBlock<*>?) {
    hideProgressBar()
    val message = exception.getMessage(context)

    block ?: return showError(message)
    showError(message, block)
}

fun DialogProvider.showError(exception: Exception) = showError(exception, null)

fun Context.createDialog(): Dialog {
    val dialog = Dialog(this)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCanceledOnTouchOutside(false)
    dialog.setCancelable(false)
    return dialog
}