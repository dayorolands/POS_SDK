package com.cluster.pos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.cluster.pos.data.PosDatabase
import com.cluster.core.ui.CreditClubFragment
import com.cluster.pos.printer.PosPrinter
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class PosFragment(layout: Int) : CreditClubFragment(layout) {
    val config: PosConfig by inject()
    val parameters: PosParameter by inject()
    val posDatabase: PosDatabase by inject()
    val printer: PosPrinter by inject { parametersOf(requireActivity(), dialogProvider) }

    protected lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        val remoteConnectionInfo = config.remoteConnectionInfo
        firebaseAnalytics.setUserProperty("default_pos_mode", localStorage.agent?.posMode)
        firebaseAnalytics.setUserProperty("pos_ip", remoteConnectionInfo.host)
        firebaseAnalytics.setUserProperty("pos_port", "${remoteConnectionInfo.port}")
    }
}
