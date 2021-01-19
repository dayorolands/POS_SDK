package com.creditclub.core.ui.widget

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.showError
import java.lang.Exception
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
interface DialogProvider {
    val activity: CreditClubActivity

    val context: Context get() = activity

    fun hideProgressBar()

    fun showError(message: CharSequence?) = showError<Nothing>(message, null)

    fun <T> showError(message: CharSequence?, block: DialogListenerBlock<T>?)

    fun <T> showInfo(message: CharSequence?, block: DialogListenerBlock<T>?)

    fun showInfo(message: CharSequence?) = showInfo<Nothing>(message, null)

    fun showSuccess(message: CharSequence?) = showSuccess<Nothing>(message, null)

    fun <T> showSuccess(message: CharSequence?, block: DialogListenerBlock<T>?)

    fun indicateError(message: CharSequence?, view: EditText?)

    fun <T> showProgressBar(
        title: CharSequence,
        message: CharSequence?,
        isCancellable: Boolean,
        block: DialogListenerBlock<T>?
    ): Dialog

    fun showProgressBar(title: CharSequence): Dialog {
        return showProgressBar<Nothing>(title, "Please wait", false, null)
    }

    fun showProgressBar(title: CharSequence, message: CharSequence?): Dialog {
        return showProgressBar<Nothing>(title, message, false, null)
    }

    fun showProgressBar(
        title: CharSequence,
        message: CharSequence?,
        block: DialogListenerBlock<Nothing>?
    ): Dialog {
        return showProgressBar(title, message, false, block)
    }

    fun requestPIN(title: CharSequence, block: DialogListenerBlock<String>)

    fun showCustomerRequestOptions(
        title: CharSequence,
        available: Array<CustomerRequestOption>,
        block: DialogListenerBlock<CustomerRequestOption>
    )

    fun showInput(params: TextFieldParams, block: DialogListenerBlock<String>)

    fun showDateInput(params: DateInputParams, block: DialogListenerBlock<LocalDate>)

    fun showOptions(
        title: CharSequence,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>
    )

    fun confirm(params: DialogConfirmParams, block: DialogListenerBlock<Boolean>?)

    fun confirm(
        title: CharSequence,
        subtitle: CharSequence?,
        block: DialogListenerBlock<Boolean>?
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

    suspend fun getSelection(title: String, options: List<DialogOptionItem>) =
        suspendCoroutine<Int?> { continuation ->
            showOptions(title, options) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(null) }
            }
        }

    suspend fun getPin(title: String) =
        suspendCoroutine<String?> { continuation ->
            requestPIN(title) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(null) }
            }
        }

    suspend fun getConfirmation(title: String, subtitle: String = "") =
        suspendCoroutine<Boolean> { continuation ->
            confirm(DialogConfirmParams(title, subtitle)) {
                onSubmit {
                    dismiss()
                    continuation.resume(it)
                }
                onClose { continuation.resume(false) }
            }
        }

    suspend fun showErrorAndWait(mesage: String) =
        suspendCoroutine<Unit> { continuation ->
            showError<Nothing>(mesage) {
                onClose { continuation.resume(Unit) }
            }
        }

    suspend fun showErrorAndWait(exception: Exception) =
        suspendCoroutine<Unit> { continuation ->
            showError<Nothing>(exception) {
                onClose { continuation.resume(Unit) }
            }
        }

    suspend fun showSuccessAndWait(message: String) =
        suspendCoroutine<Unit> { continuation ->
            showSuccess<Nothing>(message) {
                onClose { continuation.resume(Unit) }
            }
        }
}