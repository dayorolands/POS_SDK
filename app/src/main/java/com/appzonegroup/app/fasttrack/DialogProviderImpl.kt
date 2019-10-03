package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.databinding.DialogCustomerRequestOptionsBinding
import com.appzonegroup.app.fasttrack.databinding.DialogInputBinding
import com.appzonegroup.app.fasttrack.databinding.PinpadBinding
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.CalendarDialog
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.widget.*
import com.creditclub.ui.adapter.DialogOptionAdapter
import com.creditclub.ui.databinding.DialogOptionsBinding
import kotlinx.android.synthetic.main.dialog_error.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
@SuppressLint("Registered")
interface DialogProviderImpl : DialogProvider {

    val progressDialog: Dialog

    override fun hideProgressBar() {
        activity.runOnUiThread {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    override fun showError(message: String?) {
        activity.runOnUiThread {
            hideProgressBar()
            Dialogs.getErrorDialog(activity, message).show()
        }
    }

    override fun <T> showError(message: String?, block: DialogListenerBlock<T>) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getErrorDialog(activity, message)
            val dialogblock = DialogListener.create(block)

            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogblock.close()
            }

            dialog.show()
        }
    }

    override fun <T> showInfo(message: String?, block: DialogListenerBlock<T>): Dialog {
        val dialogblock = DialogListener.create(block)
        val dialog = Dialogs.getErrorDialog(activity, message)

        activity.runOnUiThread {
            hideProgressBar()
            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogblock.close()
            }

            dialog.show()
        }

        return dialog
    }

    override fun showSuccess(message: String?) {
        activity.runOnUiThread {
            hideProgressBar()
            Dialogs.getSuccessDialog(activity, message).show()
        }
    }

    override fun <T> showSuccess(message: String?, block: DialogListenerBlock<T>) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getSuccessDialog(activity, message)
            val dialogblock = DialogListener.create(block)

            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogblock.close()
            }

            dialog.show()
        }
    }

    fun renderSuccess(s: String?) {
        activity.setContentView(R.layout.layout_success)
        activity.activity.findViewById<TextView>(R.id.success_message_tv).text = s
        activity.findViewById<View>(R.id.success_close_button).setOnClickListener { activity.finish() }
    }

    override fun indicateError(message: String?, view: EditText?) {
        hideProgressBar()
        view?.also {
            view.isFocusable = true
            view.isEnabled = true
            view.error = message
            //        showNotification(message);
            view.requestFocus()
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }


    fun <T> handleProgressBar(
        title: String = activity.resources.getString(R.string.loading),
        message: String? = activity.resources.getString(R.string.please_wait),
        isCancellable: Boolean = false,
        block: DialogListenerBlock<T>? = null
    ): Dialog {
        activity.runOnUiThread {
            progressDialog.findViewById<TextView>(R.id.message_tv).text = message
            progressDialog.findViewById<TextView>(R.id.header_tv).text = title

            progressDialog.findViewById<View>(R.id.cancel_button).run {
                visibility = if (isCancellable) View.VISIBLE else View.GONE
                setOnClickListener {
                    if (progressDialog.isShowing) progressDialog.dismiss()
                }
            }

            if (block != null) {
                val listener = DialogListener.create(block)

                progressDialog.findViewById<View>(R.id.cancel_button).setOnClickListener {
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    listener.close()
                }
            } else progressDialog.setOnCancelListener { }

            if (!progressDialog.isShowing) progressDialog.show()
        }

        return progressDialog
    }

    override fun <T> showProgressBar(
        title: String,
        message: String?,
        isCancellable: Boolean,
        block: (DialogListenerBlock<T>)?
    ): Dialog {
        return handleProgressBar(title, message, isCancellable, block)
    }

    override fun showProgressBar(title: String): Dialog {
        return handleProgressBar<Nothing>(title)
    }

    override fun showProgressBar(title: String, message: String?): Dialog {
        return handleProgressBar<Nothing>(title, message)
    }

    override fun showProgressBar(
        title: String,
        message: String?,
        block: (DialogListener<Nothing>.() -> Unit)?
    ): Dialog {
        return handleProgressBar(title, message, block = block)
    }

    fun openPage(clazz: Class<*>) {
        activity.startActivity(Intent(activity, clazz))
    }

    override fun requestPIN(title: String, block: DialogListenerBlock<String>) {
        val dialog = Dialogs.getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val listener = DialogListener.create(block)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<PinpadBinding>(inflater, R.layout.pinpad, null, false)
        binding.title = title

        binding.pinChangeHandler = object : Dialogs.PinChangeHandler {
            val pin = mutableListOf<Byte>()

            override fun onChange(digit: Byte) {
                if (pin.size > 3) return

                pin.add(digit)
                binding.pinTv.text = "*".repeat(pin.size)

                if (pin.size == 4) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(500)
                        dialog.dismiss()
                        listener.submit(dialog, pin.joinToString(""))
                    }
                }
            }

            override fun onBackspacePressed(view: View) {
                pin.clear()
                binding.pinTv.text = ""
            }
        }

        dialog.setContentView(binding.root)
        binding.actionClose.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }
        dialog.setOnCancelListener { listener.close() }
        dialog.show()
    }

    override fun showCustomerRequestOptions(
        title: String,
        available: Array<CustomerRequestOption>,
        block: DialogListenerBlock<CustomerRequestOption>
    ) {
        val dialog = Dialogs.getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val listener = DialogListener.create(block)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<DialogCustomerRequestOptionsBinding>(
            inflater, R.layout.dialog_customer_request_options,
            null,
            false
        )
        binding.title = title

        if (available.contains(CustomerRequestOption.AccountNumber)) {
            binding.buttonAccountNumber.setOnClickListener {
                listener.submit(dialog, CustomerRequestOption.AccountNumber)
            }
        } else binding.buttonAccountNumber.visibility = View.GONE

        if (available.contains(CustomerRequestOption.BVN)) {
            binding.buttonBvn.setOnClickListener {
                listener.submit(dialog, CustomerRequestOption.BVN)
            }
        } else binding.buttonBvn.visibility = View.GONE


        if (available.contains(CustomerRequestOption.PhoneNumber)) {
            binding.buttonPhoneNumber.setOnClickListener {
                listener.submit(dialog, CustomerRequestOption.PhoneNumber)
            }
        } else binding.buttonPhoneNumber.visibility = View.GONE


        dialog.setContentView(binding.root)
        binding.buttonCancel.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }
        dialog.setOnCancelListener { listener.close() }
        dialog.show()
    }

    override fun showInput(params: TextFieldParams, block: DialogListenerBlock<String>) {
        val dialog = Dialogs.getDialog(activity)
        val listener = DialogListener.create(block)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val binding = DataBindingUtil.inflate<DialogInputBinding>(
            LayoutInflater.from(activity),
            R.layout.dialog_input,
            null,
            false
        )

        dialog.setContentView(binding.root)

        binding.hint = params.hint
        binding.inputType = params.type
        binding.maxLength = params.maxLength

        binding.submitButton.setOnClickListener {
            listener.submit(dialog, binding.input.text.toString())
        }

        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }

        dialog.setOnCancelListener {
            listener.close()
        }

        dialog.show()
    }

    override fun showDateInput(
        params: DateInputParams,
        block: DialogListenerBlock<LocalDate>
    ) {
        val dialog = CalendarDialog.showCalendarDialog(activity)
        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        val listener = DialogListener.create(block)

        params.maxDate?.run {
            datePicker.maxDate = atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        }
        params.minDate?.run {
            datePicker.minDate = atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        }

        dialog.findViewById<View>(R.id.calendarViewButton).setOnClickListener {
            dialog.dismiss()

            listener.submit(
                dialog,
                LocalDate.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            )
        }
    }

    override fun showOptions(
        title: String,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>
    ) {
        val dialog = Dialogs.getDialog(activity)
        val listener = DialogListener.create(block)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val binding = DataBindingUtil.inflate<DialogOptionsBinding>(
            LayoutInflater.from(activity),
            R.layout.dialog_options,
            null,
            false
        )

        dialog.setContentView(binding.root)

        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }

        binding.title = title
        binding.container.layoutManager = LinearLayoutManager(activity)
        binding.container.adapter = DialogOptionAdapter(options) {
            listener.submit(dialog, it)
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            listener.close()
        }

        dialog.show()
    }
}