package com.creditclub.core.util

import com.creditclub.core.ui.widget.DialogConfirmParams
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.ui.widget.TextFieldParams
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend inline fun DialogProvider.getInput(params: TextFieldParams) =
    suspendCoroutine<String?> { continuation ->
        showInput(params) {
            onSubmit {
                dismiss()
                continuation.resume(it)
            }
            onClose { continuation.resume(null) }
        }
    }

suspend inline fun DialogProvider.getSelection(title: String, options: List<DialogOptionItem>) =
    suspendCoroutine<Int?> { continuation ->
        showOptions(title, options) {
            onSubmit {
                dismiss()
                continuation.resume(it)
            }
            onClose { continuation.resume(null) }
        }
    }

suspend inline fun DialogProvider.getPin(title: String) =
    suspendCoroutine<String?> { continuation ->
        requestPIN(title) {
            onSubmit {
                dismiss()
                continuation.resume(it)
            }
            onClose { continuation.resume(null) }
        }
    }

suspend inline fun DialogProvider.getConfirmation(title: String, subtitle: String = "") =
    suspendCoroutine<Boolean> { continuation ->
        confirm(DialogConfirmParams(title, subtitle)) {
            onSubmit {
                dismiss()
                continuation.resume(it)
            }
            onClose { continuation.resume(false) }
        }
    }

suspend inline fun DialogProvider.showErrorAndWait(mesage: String) =
    suspendCoroutine<Unit> { continuation ->
        showError<Nothing>(mesage) {
            onClose { continuation.resume(Unit) }
        }
    }

suspend inline fun DialogProvider.showErrorAndWait(exception: Exception) =
    suspendCoroutine<Unit> { continuation ->
        showError<Nothing>(exception) {
            onClose { continuation.resume(Unit) }
        }
    }

suspend inline fun DialogProvider.showSuccessAndWait(message: String) =
    suspendCoroutine<Unit> { continuation ->
        showSuccess<Nothing>(message) {
            onClose { continuation.resume(Unit) }
        }
    }
