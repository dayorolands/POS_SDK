package com.cluster.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.R
import com.cluster.databinding.DialogConfirmBinding
import com.cluster.databinding.DialogInputBinding
import com.cluster.databinding.PinpadBinding
import com.cluster.core.ui.widget.*
import com.cluster.core.util.getMessage
import com.cluster.core.util.safeRun
import com.cluster.ui.adapter.DialogOptionAdapter
import com.cluster.ui.databinding.DialogOptionsBinding
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreditClubDialogProvider(override val context: Context) : DialogProvider {

    override val activity: Activity
        get() = if (context is Activity) context
        else throw IllegalStateException("Dialog provider context must either be a fragment or activity")

    private var currentProgressDialog: Dialog? = null

    private val progressDialog: Dialog
        get() {
            if (currentProgressDialog == null) {
                currentProgressDialog = getDialog(R.layout.dialog_progress_layout, activity)
            }
            return currentProgressDialog!!
        }

    override fun hideProgressBar() {
        if (activity.isFinishing) {
            currentProgressDialog = null
            return
        }

        activity.runOnUiThread {
            safeRun {
                if (progressDialog.isShowing && !activity.isFinishing) {
                    progressDialog.dismiss()
                }
            }
            currentProgressDialog = null
        }
    }

    override fun showError(error: Throwable, block: DialogListenerBlock<*>?) {
        val message = error.getMessage(context)

        block ?: return showError(message)
        showError(message, block)
    }

    override fun showError(message: CharSequence?, block: DialogListenerBlock<*>?) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = getErrorDialog(activity, message)

            dialog.findViewById<View>(R.id.close_btn).setOnClickListener {
                dialog.dismiss()
                if (block != null) DialogListener.create<Any>(block).close()
            }

            dialog.show()
        }
    }

    override suspend fun showErrorAndWait(message: CharSequence) {
        suspendCoroutine<Unit> { continuation ->
            activity.runOnUiThread {
                hideProgressBar()
                val dialog = getErrorDialog(activity, message).apply {
                    setOnDismissListener {
                        continuation.resume(Unit)
                    }
                }
                dialog.findViewById<View>(R.id.close_btn).setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }

    override suspend fun showErrorAndWait(title: CharSequence, message: CharSequence) {
        suspendCoroutine<Unit> { continuation ->
            activity.runOnUiThread {
                hideProgressBar()
                val dialog = getErrorDialogWithTitle(activity, title, message).apply {
                    setOnDismissListener {
                        continuation.resume(Unit)
                    }
                }
                dialog.findViewById<View>(R.id.close_btn).setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }

    override suspend fun showErrorAndWait(exception: Exception) {
        showErrorAndWait(exception.getMessage(context))
    }

    override fun showInfo(message: CharSequence?, block: DialogListenerBlock<Unit>?) {
        val dialog = getDialog(R.layout.dialog_info_success, activity)
        if (message != null)
            (dialog.findViewById(R.id.message_tv) as TextView).text = message

        dialog.findViewById<View>(R.id.ok_btn).setOnClickListener {
            dialog.dismiss()
        }
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

            dialog.findViewById<View>(R.id.close_btn).setOnClickListener {
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
        block: (DialogListenerBlock<*>)?,
    ): Dialog {
        activity.runOnUiThread {
            progressDialog.findViewById<TextView>(R.id.message_tv).text = message
            progressDialog.findViewById<TextView>(R.id.header_tv).text = title

            progressDialog.findViewById<View>(R.id.cancel_button).run {
                val showCancelButton = isCancellable || block != null
                visibility = if (showCancelButton) View.VISIBLE else View.GONE

                when {
                    block != null -> {
                        val listener = DialogListener.create<Any>(block)
                        setOnClickListener {
                            hideProgressBar()
                            listener.close()
                        }
                    }
                    isCancellable -> {
                        setOnClickListener {
                            hideProgressBar()
                        }
                    }
                    else -> progressDialog.setOnCancelListener { }
                }
            }

            if (!progressDialog.isShowing) progressDialog.show()
        }

        return progressDialog
    }

    override fun requestPIN(title: CharSequence, subtitle: CharSequence, block: DialogListenerBlock<String>) {
        val dialog = getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)

        val listener = DialogListener.create(block)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<PinpadBinding>(
            inflater,
            R.layout.pinpad, null, false
        )
        binding.title = title
        binding.subtitle = subtitle

        binding.pinChangeHandler = object : Dialogs.PinChangeHandler {
            val pin = mutableListOf<Byte>()

            override fun onSelectNumber(view: View) {
                onChange(
                    when (view.id) {
                        R.id.number1 -> 1
                        R.id.number2 -> 2
                        R.id.number3 -> 3
                        R.id.number4 -> 4
                        R.id.number5 -> 5
                        R.id.number6 -> 6
                        R.id.number7 -> 7
                        R.id.number8 -> 8
                        R.id.number9 -> 9
                        else -> 0
                    }
                )
            }

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

    override fun showInput(params: TextFieldParams, block: DialogListenerBlock<String>) {
        val dialog = getDialog(activity)
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
        block: DialogListenerBlock<LocalDate>,
    ) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.calendar_dialog)
        dialog.show()
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

    override suspend fun getDate(params: DateInputParams): LocalDate? {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.calendar_dialog)
        dialog.show()
        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)

        params.maxDate?.run {
            datePicker.maxDate = atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        }
        params.minDate?.run {
            datePicker.minDate = atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        }

        return suspendCoroutine { continuation ->
            dialog.findViewById<View>(R.id.calendarViewButton).setOnClickListener {
                dialog.dismiss()

                val localDate = LocalDate.of(
                    datePicker.year,
                    datePicker.month + 1,
                    datePicker.dayOfMonth
                )
                continuation.resume(localDate)
            }
        }
    }

    override fun showOptions(
        title: CharSequence,
        options: List<DialogOptionItem>,
        block: DialogListenerBlock<Int>,
    ) {
        activity.runOnUiThread {
            val dialog = getDialog(activity)
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
            val dialog = getDialog(context)

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

    override fun confirm(params: LoanDialogConfirmParams, block: DialogListenerBlock<Boolean>?) {
        activity.runOnUiThread {
            val dialog = getDialog(context)

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
            binding.okButton.text = params.yesButtonTex
            binding.cancelButton.text = params.noButtonTex
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
        val dialog = getDialog(R.layout.dialog_error, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }

    private fun getErrorDialogWithTitle(activity: Activity, title: CharSequence?, message: CharSequence?): Dialog {
        val dialog = getDialog(R.layout.dialog_error, activity)

        title?.also { dialog.findViewById<TextView>(R.id.error_header).text = title}
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }

        return dialog
    }

    private fun getSuccessDialog(activity: Activity, message: CharSequence?): Dialog {
        val dialog = getDialog(R.layout.dialog_success, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }

    private fun getDialog(layoutID: Int, context: Context): Dialog {
        val dialog = getDialog(context)
        dialog.setContentView(layoutID)
        return dialog
    }

    private fun getDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }
}
