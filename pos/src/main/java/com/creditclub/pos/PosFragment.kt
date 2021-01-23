package com.creditclub.pos

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.TextFieldParams
import com.creditclub.pos.printer.PosPrinter
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

@SuppressLint("Registered")
abstract class PosFragment : CreditClubFragment {
    constructor() : super()
    constructor(layout: Int) : super(layout)

    val config: PosConfig by inject()
    val parameters: PosParameter by inject()
    val callHomeService: CallHomeService by inject()
    val isoSocketHelper: IsoSocketHelper by inject()
    val posDatabase: PosDatabase by inject()
    val printer: PosPrinter by inject { parametersOf(requireActivity(), dialogProvider) }
    val PosParameter.parameters get() = managementData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    findNavController().popBackStack()
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
                    findNavController().popBackStack()
                }
            }

            dialogProvider.showError("Incorrect Password")
        }
        next(status)
    }

    inline fun supervisorAction(crossinline next: () -> Unit) {
        dialogProvider.requestPIN(getString(R.string.pos_enter_supervisor_pin)) {
            onSubmit { pin ->
                confirmSupervisorPin(pin) { passed ->
                    if (passed) next()
                }
            }
        }
    }

    inline fun adminAction(crossinline next: () -> Unit) {
        val params = TextFieldParams(hint = "Administrator password", type = "textPassword")
        dialogProvider.showInput(params) {
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

    fun showError(message: String?) = dialogProvider.showError(message)
}
