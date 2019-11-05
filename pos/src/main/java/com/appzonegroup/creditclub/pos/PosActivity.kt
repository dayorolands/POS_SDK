package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import com.appzonegroup.creditclub.pos.contract.ServiceProvider
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.service.SyncService
import com.appzonegroup.creditclub.pos.widget.Dialogs

/**
 * Created by Joseph on 1/21/2018.
 */

@SuppressLint("Registered")
abstract class PosActivity : DialogProviderActivity(), ServiceProvider {
    override val config by lazy { ConfigService.getInstance(this) }
    override val parameters by lazy { ParameterService.getInstance(this) }
    override val callHomeService by lazy { CallHomeService.getInstance(config, parameters) }
    override val isoSocketHelper by lazy { IsoSocketHelper(config, parameters) }

    val printer by lazy { PosPrinter(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        callHomeService.startCallHomeTimer()
//        callHomeService.callHome()

        if (!isMyServiceRunning(SyncService::class.java)) {
            startService(Intent(this, SyncService::class.java))
        }
    }


    fun confirmSupervisorPin(pin: String, closeOnFail: Boolean = false, next: (Boolean) -> Unit) {
        val status = pin == config.supervisorPin
        if (!status) {
            if (closeOnFail) return showError("Authentication Failed") {
                onClose {
                    finish()
                }
            }

            showError("Authentication Failed")
        }
        next(status)
    }

    fun confirmAdminPassword(password: String, closeOnFail: Boolean = false, next: (Boolean) -> Unit) {
        val status = password == config.adminPin
        if (!status) {
            if (closeOnFail) return showError("Incorrect Password") {
                onClose {
                    finish()
                }
            }

            showError("Incorrect Password")
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
                    showError(printerStatus.message) {
                        onClose {
                            finish()
                        }
                    }
                } else showError(printerStatus.message)

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

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
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
}
