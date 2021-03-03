package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.WindowManager
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.printer.PosPrinter
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

@SuppressLint("Registered")
abstract class PosActivity : CreditClubActivity {
    constructor() : super()
    constructor(layout: Int) : super(layout)
    val config: PosConfig by inject()
    val parameters: PosParameter by inject()
    val callHomeService: CallHomeService by inject()
    val isoSocketHelper: IsoSocketHelper by inject()
    val posDatabase: PosDatabase by inject()

    val printer: PosPrinter by inject { parametersOf(this, dialogProvider) }
    val PosParameter.parameters get() = managementData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (config.terminalId.isNotEmpty()) {
            callHomeService.startCallHomeTimer()
        }
    }

    inline fun confirmSupervisorPin(
        pin: String,
        closeOnFail: Boolean = false,
        crossinline next: (Boolean) -> Unit
    ) {
        val status = pin == config.supervisorPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError("Authentication Failed") {
                onClose {
                    finish()
                }
            }

            dialogProvider.showError("Authentication Failed")
        }
        next(status)
    }

    inline fun confirmAdminPassword(
        password: String,
        closeOnFail: Boolean = false,
        crossinline next: (Boolean) -> Unit
    ) {
        val status = password == config.adminPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError("Incorrect Password") {
                onClose {
                    finish()
                }
            }

            dialogProvider.showError("Incorrect Password")
        }
        next(status)
    }

    inline fun supervisorAction(crossinline next: () -> Unit) {
        Dialogs.requestPin(this, getString(R.string.pos_enter_supervisor_pin)) { pin ->
            if (pin == null) return@requestPin
            confirmSupervisorPin(pin) { passed ->
                if (passed) next()
            }
        }
    }

    inline fun adminAction(crossinline next: () -> Unit) {
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

    fun openPage(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun showError(message: String?) = dialogProvider.showError(message)

    fun showError(message: String?, block: DialogListenerBlock<*>) {
        dialogProvider.showError(message, block)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
