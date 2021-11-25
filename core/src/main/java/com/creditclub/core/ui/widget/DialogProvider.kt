package com.creditclub.core.ui.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.EditText
import androidx.annotation.StringRes
import com.creditclub.core.R
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface DialogProvider {
    val activity: Activity

    val context: Context get() = activity

    fun hideProgressBar()

    fun showError(message: CharSequence?) = showError(message, null)

    fun showError(message: CharSequence?, block: DialogListenerBlock<*>?)

    fun showError(error: Throwable) = showError(error, null)

    fun showError(error: Throwable, block: DialogListenerBlock<*>?)

    fun showInfo(message: CharSequence?, block: DialogListenerBlock<Unit>?)

    fun showInfo(message: CharSequence?) = showInfo(message, null)

    fun showSuccess(message: CharSequence?) = showSuccess(message, null)

    fun showSuccess(message: CharSequence?, block: DialogListenerBlock<*>?)

    fun indicateError(message: CharSequence?, view: EditText?)

    fun showProgressBar(
        title: CharSequence,
        message: CharSequence? = context.getString(R.string.please_wait),
        isCancellable: Boolean = false,
        block: DialogListenerBlock<*>? = null,
    ): Dialog

    fun showProgressBar(title: CharSequence): Dialog {
        return showProgressBar(title, context.getString(R.string.please_wait), false, null)
    }

    fun showProgressBar(@StringRes title: Int): Dialog {
        return showProgressBar(
            title = context.getString(title),
            message = context.getString(R.string.please_wait),
            isCancellable = false,
            block = null
        )
    }

    fun showProgressBar(title: CharSequence, message: CharSequence?): Dialog {
        return showProgressBar(title, message, false, null)
    }

    fun showProgressBar(
        title: CharSequence,
        message: CharSequence?,
        block: DialogListenerBlock<*>?,
    ): Dialog {
        return showProgressBar(title, message, true, block)
    }

    fun requestPIN(title: CharSequence, block: DialogListenerBlock<String>)

    fun showInput(params: TextFieldParams, block: DialogListenerBlock<String>)

    fun showDateInput(params: DateInputParams, block: DialogListenerBlock<LocalDate>)

    suspend fun getDate(params: DateInputParams): LocalDate?

    fun showOptions(
        title: CharSequence,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>,
    )

    fun confirm(params: DialogConfirmParams, block: DialogListenerBlock<Boolean>?)

    fun confirm(
        title: CharSequence,
        subtitle: CharSequence? = null,
        block: DialogListenerBlock<Boolean>?,
    ) = confirm(DialogConfirmParams(title, subtitle), block)

    suspend fun getInput(params: TextFieldParams) =
        suspendCoroutine<String?> { continuation ->
            showInput(params) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(null) }
            }
        }

    suspend fun getSelection(title: CharSequence, options: List<DialogOptionItem>) =
        suspendCoroutine<Int?> { continuation ->
            showOptions(title, options) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(null) }
            }
        }

    suspend fun getPin(title: CharSequence) =
        suspendCoroutine<String?> { continuation ->
            requestPIN(title) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(null) }
            }
        }

    suspend fun getPin(@StringRes title: Int) = getPin(context.getString(title))

    suspend fun getConfirmation(title: CharSequence, subtitle: CharSequence = "") =
        suspendCoroutine<Boolean> { continuation ->
            confirm(DialogConfirmParams(title, subtitle)) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(false) }
            }
        }

    suspend fun showErrorAndWait(message: CharSequence)

    suspend fun showErrorAndWait(exception: Exception)

    suspend fun showSuccessAndWait(message: CharSequence) =
        suspendCoroutine<Unit> { continuation ->
            showSuccess(message) {
                onClose { continuation.resume(Unit) }
            }
        }
}