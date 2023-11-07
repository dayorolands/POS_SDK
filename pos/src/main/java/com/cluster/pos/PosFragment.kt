package com.cluster.pos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.cluster.pos.data.PosDatabase
import com.cluster.core.ui.CreditClubFragment
import com.cluster.pos.printer.PosPrinter
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class PosFragment(layout: Int) : CreditClubFragment(layout) {
    val config: PosConfig by inject()
    val parameters: PosParameter by inject()
    val posDatabase: PosDatabase by inject()
    val printer: PosPrinter by inject { parametersOf(requireActivity(), dialogProvider) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteConnectionInfo = config.remoteConnectionInfo
        Log.d("RemoteConnectionInfo", "The remote connection info is $remoteConnectionInfo")
    }
}
