package com.creditclub.core.ui

import com.creditclub.core.ui.widget.DialogProvider

data class ValidationException(override val message: String) : IllegalArgumentException(message)

suspend inline fun DialogProvider.catchValidationErrors(crossinline block: () -> Unit): Boolean {
    val validationDidPass: Boolean = try {
        block()
        true
    } catch (exception: ValidationException) {
        showErrorAndWait(exception.message)
        false
    } catch (exception: IllegalArgumentException) {
        val message = exception.message ?: throw exception
        showErrorAndWait(message)
        false
    }
    return validationDidPass
}
