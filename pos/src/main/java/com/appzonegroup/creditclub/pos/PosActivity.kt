package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.view.MenuItem
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.core.ui.CreditClubActivity
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
    val isoSocketHelper: IsoSocketHelper by inject()
    val posDatabase: PosDatabase by inject()

    val printer: PosPrinter by inject { parametersOf(this, dialogProvider) }
    val PosParameter.parameters get() = managementData

    fun showError(message: String?) = dialogProvider.showError(message)

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
