package com.appzonegroup.creditclub.pos.provider.mpos

import android.bluetooth.BluetoothAdapter
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.PosManager
import com.creditclub.core.ui.CreditClubActivity
import com.jhl.jhlblueconn.BluetoothCommmanager
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosManager(val activity: CreditClubActivity) : PosManager {

    override val cardReader: CardReader by lazy { MPosCardReader() }

    override val sessionData = PosManager.SessionData()

    private val connectionManager by lazy { BluetoothCommmanager() }

    override fun loadEmv() {
//        connectionManager.DisConnectBlueDevice()
    }

    override fun cleanUpEmv() {
        connectionManager.closeResource()
    }

    private val stateListenerCallback = StateListenerCallback(activity.dialogProvider)

    companion object {
        val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                MPosManager(activity)
            }
        }

        fun isCompatible(): Boolean {
            return try {
                BluetoothAdapter.getDefaultAdapter() != null
            } catch (ex: Exception) {
                false
            }
        }
    }
}