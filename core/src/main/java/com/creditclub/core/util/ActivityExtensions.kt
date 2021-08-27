package com.creditclub.core.util

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/20/2019.
 * Appzone Ltd
 */

inline val Activity.finishOnClose: DialogListenerBlock<*>
    get() = {
        onClose {
            finish()
        }
    }


fun CreditClubActivity.indicateError(message: String, view: EditText?) {
    hideProgressBar()
    view?.also {
        view.isFocusable = true
        view.isEnabled = true
        view.error = message
        view.requestFocus()
    }

    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun CreditClubActivity.showError(message: String?) = dialogProvider.showError(message)

fun CreditClubActivity.showError(message: String?, block: DialogListenerBlock<*>) {
    dialogProvider.showError(message, block)
}

fun CreditClubActivity.showSuccess(message: String?) = dialogProvider.showError(message)

fun CreditClubActivity.showSuccess(message: String?, block: DialogListenerBlock<*>) {
    dialogProvider.showError(message, block)
}

fun CreditClubActivity.hideProgressBar() = dialogProvider.hideProgressBar()

fun CreditClubActivity.showProgressBar(
    title: String,
    subtitle: String = "Please wait...",
    isCancellable: Boolean = false,
    block: DialogListenerBlock<*>? = null
): Dialog {
    return dialogProvider.showProgressBar(title, subtitle, isCancellable, block)
}