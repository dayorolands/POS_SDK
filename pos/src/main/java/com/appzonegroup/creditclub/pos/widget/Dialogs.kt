package com.appzonegroup.creditclub.pos.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.databinding.PosDialogConfirmBinding
import com.appzonegroup.creditclub.pos.databinding.PosDialogInputBinding
import com.appzonegroup.creditclub.pos.databinding.PosPinpadBinding
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by Joseph on 6/5/2016.
 */

typealias ListenerBlock<T> = Dialogs.Listener<T>.() -> Unit

object Dialogs {

    fun getProgress(activity: Context, header: String?): Dialog {
        val dialog = getDialog(R.layout.dialog_progress_layout, activity)
        header?.also { dialog.findViewById<TextView>(R.id.header_tv).text = header }

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

    private fun getDialog(layoutID: Int, context: Context): Dialog {
        val dialog = getDialog(context)
        dialog.setContentView(layoutID)
        return dialog
    }

    fun getErrorDialog(activity: Activity, message: String?): Dialog {
        val dialog = getDialog(R.layout.pos_dialog_error, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }

    fun getSuccessDialog(activity: Activity, message: String?): Dialog {
        val dialog = getDialog(R.layout.dialog_success, activity)
        message?.also { dialog.findViewById<TextView>(R.id.message_tv).text = message }
        dialog.findViewById<View>(R.id.close_btn).setOnClickListener { dialog.dismiss() }

        return dialog
    }

//    fun getProgressDialog(activity: Activity, msg: String): ProgressDialog {
//        val progressDialog = ProgressDialog(activity)
//        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        progressDialog.setMessage(msg)
//        progressDialog.show()
//
//        return progressDialog
//    }
//
//    fun getAlertDialog(context: Context, msg: String): AlertDialog {
//        val builder1 = AlertDialog.Builder(context)
//        builder1.setMessage(msg)
//        builder1.setCancelable(true)
//
//        builder1.setPositiveButton(
//            "Ok"
//        ) { dialog, id -> dialog.cancel() }
//
//
//        return builder1.create()
//    }

    fun requestPin(
        context: Activity,
        title: String = "Enter Pin",
        timeout: Long = 10000,
        next: (String?) -> Unit
    ): Dialog {

        val dialog = getDialog(context)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val binding =
            DataBindingUtil.inflate<PosPinpadBinding>(LayoutInflater.from(context), R.layout.pos_pinpad, null, false)
        binding.title = title

        var isTimerRunning = true

        val createTimer: () -> TimerTask? = {
            if (timeout <= 0) null
            else Timer().schedule(timeout) {
                context.runOnUiThread {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        next(null)
                    }
                }
            }
        }

        var timeoutTimer: TimerTask? = createTimer()

        val stopTimer = {
            isTimerRunning = false
            timeoutTimer?.cancel()
            timeoutTimer = null
        }

        val restartTimer = {
            if (!isTimerRunning) {
                isTimerRunning = true
                timeoutTimer = createTimer()
            } else {
                stopTimer()
                timeoutTimer = createTimer()
            }
        }

        binding.pinChangeHandler = object : PinChangeHandler {
            val pin = mutableListOf<Byte>()

            override fun onChange(digit: Byte) {
                restartTimer()
                if (pin.size > 7) return

                pin.add(digit)
                binding.pinTv.text = "*".repeat(pin.size)
            }

            override fun onBackspacePressed(view: View) {
//                if (pin.size == 0) return
//                pin.removeAt(pin.size - 1)
//                binding.pinTv.text = ".".repeat(pin.size)
                restartTimer()
                pin.clear()
                binding.pinTv.text = ""
            }

            override fun onEnterPressed(view: View) {
                stopTimer()
                dialog.dismiss()
                next(pin.joinToString(""))
            }
        }

        dialog.setContentView(binding.root)
        binding.actionClose.setOnClickListener {
            dialog.dismiss()
            next(null)
        }
        dialog.setOnCancelListener { next(null) }
        dialog.show()

        return dialog
    }


    fun input(
        activity: Activity,
        hint: String,
        type: Int = InputType.TYPE_CLASS_TEXT,
        initialValue: String? = "",
        config: ListenerBlock<String>? = null
    ): Dialog {
        val dialog = getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val binding = DataBindingUtil.inflate<PosDialogInputBinding>(
            LayoutInflater.from(activity),
            R.layout.pos_dialog_input,
            null,
            false
        )
        dialog.setContentView(binding.root)
        binding.inputLayout.hint = hint
        binding.input.setText(initialValue)
        binding.input.inputType = type
        binding.submitButton.setOnClickListener {
            if (config != null) Dialogs.Listener<String>().apply(config).submit(
                dialog,
                dialog.findViewById<EditText>(R.id.input).text.toString()
            )
        }
        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            if (config != null) Dialogs.Listener<String>().apply(config).close()
        }
        dialog.setOnCancelListener {
            if (config != null) Dialogs.Listener<String>().apply(config).close()
        }
//        dialog.setOnShowListener { binding.input.requestFocus() }
        dialog.show()

        return dialog
    }

    fun confirm(
        context: Context,
        title: String = "Confirm",
        subtitle: String = "Are you sure?",
        config: ListenerBlock<Boolean>? = null
    ): Dialog {
        val dialog = getDialog(context)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val dialogConfig by lazy {
            if (config != null) Listener<Boolean>().apply(config)
            else null
        }
        val binding = DataBindingUtil.inflate<PosDialogConfirmBinding>(
            LayoutInflater.from(context),
            R.layout.pos_dialog_confirm,
            null,
            false
        )
        dialog.setContentView(binding.root)
        binding.title = title
        binding.subtitle = subtitle
        binding.okButton.setOnClickListener {
            if (config != null) dialogConfig?.submit(
                dialog,
                true
            )
        }
        binding.cancelButton.setOnClickListener {
            if (config != null) {
                dialog.dismiss()
                dialogConfig?.close()
            }
        }
        dialog.setOnCancelListener {
            if (config != null) dialogConfig?.close()
        }

        return dialog
    }

    fun date(context: Context, config: ListenerBlock<Array<String>>? = null): Dialog {
        val dialog = getDialog(context)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.pos_dialog_calender)
        val calendarViewButton = dialog.findViewById<View>(R.id.calendarViewButton)
        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        calendarViewButton.setOnClickListener {
            if (config != null) {
                val dayOfMonth = datePicker.dayOfMonth
                val month = datePicker.month + 1

                val dayString = if (dayOfMonth > 9) dayOfMonth.toString() + "" else "0$dayOfMonth"
                val monthString = if (month > 9) month.toString() + "" else "0$month"
                val yearString = datePicker.year.toString()

                dialog.dismiss()
                Dialogs.Listener<Array<String>>().apply(config)
                    .submit(dialog, arrayOf(dayString, monthString, yearString))
            }
        }
        dialog.setOnCancelListener {
            if (config != null) Dialogs.Listener<Array<String>>().apply(config).close()
        }
        return dialog
    }

    class Listener<T> {
        private var closeListener: (() -> Unit)? = null

        fun onClose(next: () -> Unit) {
            closeListener = next
        }

        fun close() {
            closeListener?.invoke()
        }


        private var submitListener: (Dialog.(T) -> Unit)? = null

        fun onSubmit(next: Dialog.(T) -> Unit) {
            submitListener = next
        }

        fun submit(dialog: Dialog, result: T) {
            submitListener?.invoke(dialog, result)
        }
    }

    interface PinChangeHandler {
        fun onSelectNumber(view: View) {
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

        fun onBackspacePressed(view: View)

        fun onEnterPressed(view: View)

        fun onChange(digit: Byte)
    }
}
