package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.appzonegroup.creditclub.pos.contract.ServiceProvider
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock

@SuppressLint("Registered")
abstract class PosActivity : CreditClubActivity(), ServiceProvider {
    override val config by lazy { ConfigService.getInstance(this) }
    override val parameters by lazy { ParameterService.getInstance(this) }
    override val callHomeService by lazy { CallHomeService.getInstance(config, parameters, this) }
    override val isoSocketHelper by lazy { IsoSocketHelper(config, parameters, this) }

    val printer by lazy { PosPrinter(this, dialogProvider) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (config.terminalId.isNotEmpty()) {
            callHomeService.startCallHomeTimer()
        }
    }


    fun confirmSupervisorPin(pin: String, closeOnFail: Boolean = false, next: (Boolean) -> Unit) {
        val status = pin == config.supervisorPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError<Nothing>("Authentication Failed") {
                onClose {
                    finish()
                }
            }

            dialogProvider.showError("Authentication Failed")
        }
        next(status)
    }

    private fun confirmAdminPassword(
        password: String,
        closeOnFail: Boolean = false,
        next: (Boolean) -> Unit
    ) {
        val status = password == config.adminPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError<Nothing>("Incorrect Password") {
                onClose {
                    finish()
                }
            }

            dialogProvider.showError("Incorrect Password")
        }
        next(status)
    }

    fun supervisorAction(next: () -> Unit) {
        Dialogs.requestPin(this, getString(R.string.pos_enter_supervisor_pin)) { pin ->
            if (pin == null) return@requestPin
            confirmSupervisorPin(pin) { passed ->
                if (passed) next()
            }
        }
    }

    fun printerDependentAction(closeOnFail: Boolean = false, block: () -> Unit) {
        printer.checkAsync { printerStatus ->
            if (printerStatus != PrinterStatus.READY) {
                if (closeOnFail) {
                    dialogProvider.showError<Nothing>(printerStatus.message) {
                        onClose {
                            finish()
                        }
                    }
                } else dialogProvider.showError(printerStatus.message)

                return@checkAsync
            }

            block()
        }
    }

    fun adminAction(next: () -> Unit) {
        val passwordType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        Dialogs.input(this, "Administrator password", passwordType) {
            onSubmit { password ->
                dismiss()
                confirmAdminPassword(password) { passed ->
                    if (passed) next()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        callHomeService.startCallHomeTimer()
    }

    fun startActivity(classToStart: Class<*>) {
        startActivity(Intent(this, classToStart))
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager?
        manager?.run {
            for (service in getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }

        return false
    }

    fun openPage(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun indicateError(message: String, view: EditText?) {
        hideProgressBar()
        view?.also {
            view.isFocusable = true
            view.isEnabled = true
            view.error = message
            view.requestFocus()
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showError(message: String?) = dialogProvider.showError(message)

    fun showError(message: String?, block: DialogListenerBlock<Nothing>) {
        dialogProvider.showError(message, block)
    }

    fun showSuccess(message: String?) = dialogProvider.showError(message)

    fun showSuccess(message: String?, block: DialogListenerBlock<Nothing>) {
        dialogProvider.showError(message, block)
    }

    fun hideProgressBar() = dialogProvider.hideProgressBar()

    fun showProgressBar(
        title: String,
        subtitle: String = "Please wait...",
        isCancellable: Boolean = false,
        block: DialogListenerBlock<Nothing>? = null
    ): Dialog {
        return dialogProvider.showProgressBar(title, subtitle, isCancellable, block)
    }
}
