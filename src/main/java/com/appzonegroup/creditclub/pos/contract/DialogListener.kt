package com.appzonegroup.creditclub.pos.contract

import android.app.Dialog

typealias DialogListenerBlock<T> = DialogListener<T>.() -> Unit

class DialogListener<T> {
    private var closeListener: (() -> Unit)? = null
    private var submitListener: (Dialog.(T) -> Unit)? = null

    fun onClose(next: () -> Unit) {
        closeListener = next
    }

    fun close() {
        closeListener?.invoke()
    }

    fun onSubmit(next: Dialog.(T) -> Unit) {
        submitListener = next
    }

    fun submit(dialog: Dialog, result: T) {
        submitListener?.invoke(dialog, result)
    }
}