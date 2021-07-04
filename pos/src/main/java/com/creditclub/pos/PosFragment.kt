package com.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.pos.printer.PosPrinter
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class PosFragment(layout: Int) : CreditClubFragment(layout) {
    val config: PosConfig by inject()
    val parameters: PosParameter by inject()
    val posDatabase: PosDatabase by inject()
    val printer: PosPrinter by inject { parametersOf(requireActivity(), dialogProvider) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (config.terminalId.isNotBlank()) {
            val callHomeService: CallHomeService = get()
            callHomeService.startCallHomeTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (config.terminalId.isNotBlank()) {
            val callHomeService: CallHomeService = get()
            callHomeService.startCallHomeTimer()
        }
    }
}
