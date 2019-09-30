package com.appzonegroup.app.fasttrack.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.appzonegroup.app.fasttrack.databinding.*
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.CustomerAccount
import com.appzonegroup.app.fasttrack.network.ApiServiceObject
import com.appzonegroup.app.fasttrack.utility.CustomerHelper
import com.creditclub.core.util.localStorage
import kotlinx.android.synthetic.main.dialog_calender.*
import kotlinx.android.synthetic.main.dialog_input.*
import kotlinx.coroutines.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

object Dialogs {

    fun getProgress(activity: Context, header: String?): Dialog {
        val dialog = getDialog(R.layout.dialog_progress_layout, activity)
        header?.also { dialog.findViewById<TextView>(R.id.header_tv).text = header }

        return dialog
    }

    fun getDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    fun getDialog(layoutID: Int, context: Context): Dialog {
        val dialog = getDialog(context)
        dialog.setContentView(layoutID)
        return dialog
    }

    fun getErrorDialog(activity: Activity, message: String?): Dialog {
        val dialog = getDialog(R.layout.dialog_error, activity)
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

    fun requestPin(context: Activity, title: String = "Enter PIN", next: (String?) -> Unit) {

        val dialog = getDialog(context)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val binding =
                DataBindingUtil.inflate<PinpadBinding>(LayoutInflater.from(context), R.layout.pinpad, null, false)
        binding.title = title

        binding.pinChangeHandler = object : PinChangeHandler {
            val pin = mutableListOf<Byte>()

            override fun onChange(digit: Byte) {
                if (pin.size > 3) return

                pin.add(digit)
                binding.pinTv.text = "*".repeat(pin.size)

                if (pin.size == 4) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(500)
                        dialog.dismiss()
                        next(pin.joinToString(""))
                    }
                }
            }

            override fun onBackspacePressed(view: View) {
//                if (pin.size == 0) return
//                pin.removeAt(pin.size - 1)
//                binding.pinTv.text = ".".repeat(pin.size)

                pin.clear()
                binding.pinTv.text = ""
            }
        }

        dialog.setContentView(binding.root)
        binding.actionClose.setOnClickListener {
            dialog.dismiss()
            next(null)
        }
        dialog.setOnCancelListener { next(null) }
        dialog.show()
    }


    fun input(
        activity: DialogProvider,
        hint: String,
        type: Int = InputType.TYPE_CLASS_NUMBER,
        config: DialogListenerBlock<String>? = null
    ): Dialog {
        val dialog = getDialog(activity.activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        val binding = DataBindingUtil.inflate<DialogInputBinding>(
                LayoutInflater.from(activity.activity),
                R.layout.dialog_input,
                null,
                false
        )
        dialog.setContentView(binding.root)
        binding.inputLayout.hint = hint
        binding.input.inputType = type
        binding.submitButton.setOnClickListener {
            if (config != null) DialogListener<String>().apply(config).submit(
                    dialog,
                    dialog.input.text.toString()
            )
        }
        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
            if (config != null) DialogListener<String>().apply(config).close()
        }
        dialog.setOnCancelListener {
            if (config != null) DialogListener<String>().apply(config).close()
        }
        dialog.show()
//        dialog.setOnShowListener { binding.input.requestFocus() }

        return dialog
    }

    fun confirm(
            context: Context,
            title: String = "Confirm",
            subtitle: String = "Are you sure?",
            config: DialogListenerBlock<Boolean>? = null
    ): Dialog {
        val dialog = getDialog(context)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val dialogConfig by lazy {
            if (config != null) DialogListener.create(config)
            else null
        }
        val binding = DataBindingUtil.inflate<DialogConfirmBinding>(
                LayoutInflater.from(context),
                R.layout.dialog_confirm,
                null,
                false
        )
        dialog.setContentView(binding.root)
        binding.title = title
        binding.subtitle = subtitle
        binding.okButton.setOnClickListener {
            if (config != null) {
                dialog.dismiss()
                dialogConfig?.submit(dialog, true)
            }
        }
        binding.cancelButton.setOnClickListener {
            if (config != null) {
                dialog.dismiss()
                dialogConfig?.close()
            }
        }
        dialog.setOnCancelListener {
            if (config != null) {
                dialog.dismiss()
                dialogConfig?.close()
            }
        }
        dialog.show()

        return dialog
    }

    fun date(activity: DialogProvider, config: DialogListenerBlock<Array<String>>? = null): Dialog {
        val dialog = getDialog(activity.activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_calender)
        dialog.calendarViewButton.setOnClickListener {
            if (config != null) {
                val dayOfMonth = dialog.datePicker.dayOfMonth
                val month = dialog.datePicker.month + 1

                val dayString = if (dayOfMonth > 9) dayOfMonth.toString() + "" else "0$dayOfMonth"
                val monthString = if (month > 9) month.toString() + "" else "0$month"
                val yearString = dialog.datePicker.year.toString()

                dialog.dismiss()
                DialogListener<Array<String>>().apply(config)
                        .submit(dialog, arrayOf(dayString, monthString, yearString))
            }
        }
        dialog.setOnCancelListener {
            if (config != null) DialogListener<Array<String>>().apply(config).close()
        }
        return dialog
    }

    fun getCustomer(dialogProvider: DialogProvider, title: String = "Select Customer", config: DialogListenerBlock<ApiServiceObject.ApiResult<CustomerAccount>>) {
        val dialog = getDialog(dialogProvider.activity)
        val binding = DataBindingUtil.inflate<DialogCustomerDetailsBinding>(
                LayoutInflater.from(dialogProvider.activity),
                R.layout.dialog_customer_details,
                null,
                false
        )
        val listener by lazy { DialogListener<ApiServiceObject.ApiResult<CustomerAccount>>().apply(config) }

        binding.title = title
        binding.accountNumberButton.setOnClickListener {
            dialog.dismiss()
            Dialogs.input(dialogProvider, "Account Number") {
                onSubmit { accountNumber ->
                    dismiss()

                    val institutionCode = dialogProvider.activity.localStorage.institutionCode
                    val url = "${AppConstants.getBaseUrl()}/CreditClubMiddleWareAPI/CreditClubStatic/GetCustomerAccountByPhoneNumber?phoneNumber=$accountNumber&institutionCode=$institutionCode"

                    GlobalScope.launch(Dispatchers.Main) {
                        dialogProvider.showProgressBar("Getting customer details")
                        val customerJson = withContext(Dispatchers.Default) {
                            ApiServiceObject.get(url)
                        }
                        dialogProvider.hideProgressBar()
                        val customer = CustomerHelper.processCustomerAccountInfo(customerJson.value, dialogProvider)
                                ?: return@launch

                        listener.submit(this@onSubmit, ApiServiceObject.ApiResult(customer, null))
                    }
                }
            }.show()
        }


        binding.phoneNumberButton.setOnClickListener {

            dialog.dismiss()
            Dialogs.input(dialogProvider, "Phone Number") {
                onSubmit { phoneNumber ->
                    dismiss()
                    val institutionCode = dialogProvider.activity.localStorage.institutionCode
                    val url="${AppConstants.getBaseUrl()}/CreditClubMiddleWareAPI/CreditClubStatic/GetCustomerAccountByPhoneNumber?phoneNumber=$phoneNumber&institutionCode=$institutionCode"

                    GlobalScope.launch(Dispatchers.Main) {
                        dialogProvider.showProgressBar("Getting customer details")
                        val customerJson = withContext(Dispatchers.Default) {
                            ApiServiceObject.get(url)
                        }
                        dialogProvider.hideProgressBar()
                        val customer = CustomerHelper.processCustomerAccount(customerJson.value, dialogProvider)
                                ?: return@launch

                        listener.submit(this@onSubmit, ApiServiceObject.ApiResult(customer, null))
                    }
                }
            }.show()
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnCancelListener {
            listener.close()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }


    @JvmOverloads
    fun showCustomerIdDialog(activity: Activity, hint: String, listener: OnSubmitClickListener, type: Int = InputType.TYPE_CLASS_NUMBER): Dialog {
        val dialog = getCustomerIdDialog(activity, hint, listener, type)
        dialog.show()
        return dialog
    }

    @JvmOverloads
    fun showCustomerIdDialog(activity: Activity, hint: String, type: Int = InputType.TYPE_CLASS_NUMBER, listener: (Dialog, View) -> Unit): Dialog {
        val dialog = getCustomerIdDialog(activity, hint, object : OnSubmitClickListener {
            override fun onSubmitClick(dialog: Dialog, view: View) {
                listener(dialog, view)
            }

        }, type)
        dialog.show()
        return dialog
    }

    fun getCustomerIdDialog(activity: Activity, hint: String, listener: OnSubmitClickListener?, type: Int): Dialog {
        val dialog = getDialog(activity)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val binding = DataBindingUtil.inflate<FindCustomerInputBinding>(LayoutInflater.from(activity), R.layout.find_customer_input, null, false)
        dialog.setContentView(binding.root)

//        binding.input.setText(if (hint == "Enter Account Number") "1100030572" else "08132470623")

        binding.inputLayout.hint = hint
        binding.input.inputType = type
        binding.submitButton.setOnClickListener { view ->
            listener?.onSubmitClick(dialog, view)
        }
        dialog.setOnShowListener { binding.input.requestFocus() }

        return dialog
    }


    fun getInformationDialog(activity: Activity, message: String?, shouldClose: Boolean): Dialog {
        val dialog = getDialog(R.layout.dialog_info_success, activity)
        if (message != null)
            (dialog.findViewById(R.id.message_tv) as TextView).text = message

        dialog.findViewById<View>(R.id.ok_btn).setOnClickListener {
            dialog.dismiss()
            if (shouldClose)
                activity.finish()
        }

        return dialog
    }


    interface OnSubmitClickListener {
        fun onSubmitClick(dialog: Dialog, view: View)
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

        fun onChange(digit: Byte)
    }
}