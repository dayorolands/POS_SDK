package com.appzonegroup.creditclub.pos.contract

import android.app.Dialog
import android.widget.EditText

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
interface DialogProvider {
    fun hideProgressBar()

    fun showError(message: String)

    fun showError(message: String, next: DialogListener<Nothing>.() -> Unit)

    fun showInfo(message: String, next: DialogListenerBlock<Nothing>): Dialog

    fun showSuccess(message: String)

    fun showSuccess(message: String, next: DialogListener<Nothing>.() -> Unit)

    fun indicateError(message: String, view: EditText?)

    fun showProgressBar(
        title: String,
        message: String?,
        isCancellable: Boolean,
        config: (DialogListener<Nothing>.() -> Unit)?
    ): Dialog

    fun showProgressBar(title: String): Dialog

    fun showProgressBar(title: String, message: String?): Dialog

    fun showProgressBar(
        title: String,
        message: String?,
        config: (DialogListener<Nothing>.() -> Unit)?
    ): Dialog
}