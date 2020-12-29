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

    fun showError(message: CharSequence?) = showError<Nothing>(message, null)

    fun <T> showError(message: CharSequence?, block: DialogListenerBlock<T>?)

    fun <T> showInfo(message: CharSequence?, block: DialogListenerBlock<T>?)

    fun showInfo(message: CharSequence?) = showInfo<Nothing>(message, null)

    fun showSuccess(message: CharSequence?)

    fun <T> showSuccess(message: CharSequence?, block: DialogListenerBlock<T>)

    fun indicateError(message: CharSequence?, view: EditText?)

    fun <T> showProgressBar(
        title: CharSequence,
        message: CharSequence?,
        isCancellable: Boolean,
        block: DialogListenerBlock<T>?
    ): Dialog

    fun showProgressBar(title: CharSequence): Dialog {
        return showProgressBar<Nothing>(title, null, false, null)
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

    fun requestPIN(title: CharSequence, block: DialogListenerBlock<String>) {
        showError("PIN dialog not implemented", block)
    }

    fun requestAgentPIN(title: CharSequence, block: DialogListenerBlock<String>) {
        requestPIN("Enter agent PIN", block)
    }

    fun requestCustomerPIN(title: CharSequence, block: DialogListenerBlock<String>) {
        requestPIN("Enter Customer PIN", block)
    }

    fun showCustomerRequestOptions(
        title: CharSequence,
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
        title: CharSequence,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>
    ) {
        showError("Option dialog not implemented", block)
    }

    fun confirm(params: DialogConfirmParams, block: DialogListenerBlock<Boolean>?) {
        showError("Confirmation dialog not implemented", block)
    }

    fun confirm(
        title: CharSequence,
        subtitle: CharSequence?,
        block: DialogListenerBlock<Boolean>?
    ) = confirm(DialogConfirmParams(title, subtitle), block)
}