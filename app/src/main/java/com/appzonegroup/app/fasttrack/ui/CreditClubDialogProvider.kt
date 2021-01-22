package com.appzonegroup.app.fasttrack.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.DialogConfirmBinding
import com.appzonegroup.app.fasttrack.databinding.DialogCustomerRequestOptionsBinding
import com.appzonegroup.app.fasttrack.databinding.DialogInputBinding
import com.appzonegroup.app.fasttrack.databinding.PinpadBinding
import com.appzonegroup.app.fasttrack.utility.CalendarDialog
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.*
import com.creditclub.core.util.safeRun
import com.creditclub.ui.adapter.DialogOptionAdapter
import com.creditclub.ui.databinding.DialogOptionsBinding
import kotlinx.android.synthetic.main.dialog_error.*
import java.time.LocalDate
import java.time.ZoneId

class CreditClubDialogProvider(override val context: Context) : DialogProvider {

    override val activity: CreditClubActivity
        get() = when (context) {
            is CreditClubActivity -> context
            is Fragment -> context.activity as CreditClubActivity
            else -> throw IllegalStateException("Dialog provider context must either be a fragment or activity")
        }

    private var currentProgressDialog: Dialog? = null

    private val progressDialog: Dialog
        get() {
            if (currentProgressDialog == null) {
                currentProgressDialog = Dialogs.getProgress(activity, null)
            }
            return currentProgressDialog!!
        }

    override fun hideProgressBar() {
        if (!activity.isFinishing) activity.runOnUiThread {
            safeRun {
                if (progressDialog.isShowing && !activity.isFinishing) {
                    progressDialog.dismiss()
                }
            }
            currentProgressDialog = null
        } else currentProgressDialog = null
    }

    override fun showError(message: CharSequence?, block: DialogListenerBlock<*>?) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = getErrorDialog(activity, message)

            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                if (block != null) DialogListener.create<Any>(block).close()
            }

            dialog.show()
        }
    }

    override fun <T> showInfo(message: CharSequence?, block: DialogListenerBlock<T>?) {
        val dialog = Dialogs.getInformationDialog(activity, message, false)

        activity.runOnUiThread {
            hideProgressBar()
            dialog.findViewById<View>(R.id.ok_btn).setOnClickListener {
                dialog.dismiss()
                block?.build()?.close()
            }

            dialog.show()
        }
    }

    override fun showSuccess(message: CharSequence?, block: DialogListenerBlock<*>?) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = getSuccessDialog(activity, message)

            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                if (block != null) DialogListener.create<Any>(block).close()
            }

            dialog.show()
        }
    }

    override fun indicateError(message: CharSequence?, view: EditText?) {
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

    override fun showProgressBar(
        title: CharSequence,
        message: CharSequence?,
        isCancellable: Boolean,
        block: (DialogListenerBlock<*>)?
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
                val listener = DialogListener.create<Any>(block)

                progressDialog.findViewById<View>(R.id.cancel_button).setOnClickListener {
                    if (progressDialog.isShowing) progressDialog.hide()
                    listener.close()
                }
            } else progressDialog.setOnCancelListener { }

            if (!progressDialog.isShowing) progressDialog.show()
        }

        return progressDialog
    }

    override fun requestPIN(title: CharSequence, block: DialogListenerBlock<String>) {
        val dialog = Dialogs.getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val listener = DialogListener.create(block)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<PinpadBinding>(
            inflater,
            R.layout.pinpad, null, false
        )
        binding.title = title

        binding.pinChangeHandler = object : Dialogs.PinChangeHandler {
            val pin = mutableListOf<Byte>()

            override fun onChange(digit: Byte) {
                if (pin.size > 3) return

                pin.add(digit)
                binding.pinTv.text = "*".repeat(pin.size)
            }

            override fun onBackspacePressed(view: View) {
                pin.clear()
                binding.pinTv.text = ""
            }

            override fun onEnterPressed(view: View) {
                dialog.dismiss()
                listener.submit(dialog, pin.joinToString(""))
            }
        }

        dialog.setContentView(binding.root)
        binding.actionClose.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }
        dialog.setOnCancelListener {
            dialog.dismiss()
            listener.close()
        }
        dialog.show()
    }

    override fun showCustomerRequestOptions(
        title: CharSequence,
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
        dialog.setOnCancelListener {
            dialog.dismiss()
            listener.close()
        }
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
        binding.helperText = params.helperText
        binding.input.setText(params.initialValue)
        binding.input.doOnTextChanged { _, _, _, _ ->
            binding.inputLayout.error = null
        }

        if (params.maxLength > 0) {
            binding.inputLayout.counterMaxLength = params.maxLength
            binding.inputLayout.isCounterEnabled = true
        }

        binding.submitButton.setOnClickListener {
            val value = binding.input.text?.trim { it <= ' ' }?.toString() ?: ""
            val typePlural = when (params.type) {
                "number", "numberPassword" -> "digits"
                else -> "characters"
            }
            if (params.required && value.isBlank()) {
                binding.inputLayout.error = "is required"
                binding.inputLayout.refreshErrorIconDrawableState()
                return@setOnClickListener
            }
            if (params.minLength > value.length) {
                binding.inputLayout.error = "must not be less than ${params.minLength} $typePlural"
                return@setOnClickListener
            }

            listener.submit(dialog, value)
        }

        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            listener.close()
        }

        dialog.setOnCancelListener {
            dialog.dismiss()
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
        title: CharSequence,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>
    ) {
        activity.runOnUiThread {
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
                dialog.dismiss()
                listener.submit(dialog, it)
            }

            dialog.setOnCancelListener {
                dialog.dismiss()
                listener.close()
            }

            dialog.show()
        }
    }

    override fun confirm(params: DialogConfirmParams, block: DialogListenerBlock<Boolean>?) {
        activity.runOnUiThread {
            val dialog = Dialogs.getDialog(context)

            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(false)

            val listener by lazy {
                if (block != null) DialogListener.create(block)
                else null
            }
            val binding = DataBindingUtil.inflate<DialogConfirmBinding>(
                LayoutInflater.from(context),
                R.layout.dialog_confirm,
                null,
                false
            )
            dialog.setContentView(binding.root)
            binding.title = params.title
            binding.subtitle = params.subtitle
            binding.okButton.setOnClickListener {
                if (block != null) {
                    dialog.dismiss()
                    listener?.submit(dialog, true)
                }
            }
            binding.cancelButton.setOnClickListener {
                if (block != null) {
                    dialog.dismiss()
                    listener?.close()
                }
            }

            dialog.setOnCancelListener {
                if (block != null) {
                    dialog.dismiss()
                    listener?.close()
                }
            }

            dialog.show()
        }
    }

    private fun getErrorDialog(activity: Activity, message: CharSequence?): Dialog {
        val dialog = Dialogs.getDialog(R.layout.dialog_error, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }

    private fun getSuccessDialog(activity: Activity, message: CharSequence?): Dialog {
        val dialog = Dialogs.getDialog(R.layout.dialog_success, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }
}
