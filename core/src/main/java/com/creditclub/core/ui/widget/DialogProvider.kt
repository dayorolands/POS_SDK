package com.creditclub.core.ui.widget

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.CreditClubActivity
import java.time.LocalDate

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
interface DialogProvider {
    val activity: CreditClubActivity

    val context: Context get() = activity

    fun hideProgressBar()

    fun showError(message: String?)

    fun <T> showError(message: String?, block: DialogListenerBlock<T>?)

    fun <T> showInfo(message: String?, block: DialogListenerBlock<T>?)

    fun showInfo(message: String?) = showInfo<Nothing>(message, null)

    fun showSuccess(message: String?)

    fun <T> showSuccess(message: String?, block: DialogListenerBlock<T>)

    fun indicateError(message: String?, view: EditText?)

    fun <T> showProgressBar(
        title: String,
        message: String?,
        isCancellable: Boolean,
        block: DialogListenerBlock<T>?
    ): Dialog

    fun showProgressBar(title: String): Dialog

    fun showProgressBar(title: String, message: String?): Dialog

    fun showProgressBar(
        title: String,
        message: String?,
        block: DialogListenerBlock<Nothing>?
    ): Dialog

    fun requestPIN(title: String, block: DialogListenerBlock<String>) {
        showError("PIN dialog not implemented", block)
    }

    fun requestAgentPIN(title: String, block: DialogListenerBlock<String>) {
        requestPIN("Enter agent PIN", block)
    }

    fun requestCustomerPIN(title: String, block: DialogListenerBlock<String>) {
        requestPIN("Enter Customer PIN", block)
    }

    fun showCustomerRequestOptions(
        title: String,
        available: Array<CustomerRequestOption>,
        block: DialogListenerBlock<CustomerRequestOption>
    ) {
        showError("Customer Request dialog not implemented", block)
    }

    fun showInput(params: TextFieldParams, block: DialogListenerBlock<String>) {
        showError("Input dialog not implemented", block)
    }

    fun showDateInput(params: DateInputParams, block: DialogListenerBlock<LocalDate>) {
        showError("Calendar input not implemented", block)
    }

    fun showOptions(
        title: String,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>
    ) {
        showError("Option dialog not implemented", block)
    }

    fun confirm(params: DialogConfirmParams, block: DialogListenerBlock<Boolean>?) {
        showError("Confirmation dialog not implemented", block)
    }

    fun confirm(title: String, subtitle: String?, block: DialogListenerBlock<Boolean>?) {
        confirm(DialogConfirmParams(title, subtitle ?: ""), block)
    }
}