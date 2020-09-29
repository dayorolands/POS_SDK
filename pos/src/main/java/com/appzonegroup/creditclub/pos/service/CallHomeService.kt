package com.appzonegroup.creditclub.pos.service

import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.models.messaging.NetworkManagement
import com.appzonegroup.creditclub.pos.models.messaging.isoMsg
import com.creditclub.pos.PosConfig
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.concurrent.schedule


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/3/2019.
 * Appzone Ltd
 */

class CallHomeService : KoinComponent {

    private val posConfig: PosConfig by inject()
    private val period get() = 1000L * posConfig.callHome.toLong()
    private var isCallHomeTimerRunning = false
    private val connection: IsoSocketHelper by inject()
    private var callHomeTask = createTimer()

    fun startCallHomeTimer() {
        if (!isCallHomeTimerRunning) {
            isCallHomeTimerRunning = !isCallHomeTimerRunning
            callHomeTask = createTimer()
        }
    }

    fun stopCallHomeTimer() {
        if (isCallHomeTimerRunning) {
            isCallHomeTimerRunning = !isCallHomeTimerRunning
            callHomeTask.cancel()
        }
    }

    fun callHome() {
        val message = isoMsg(::NetworkManagement) {
            processingCode3 = "9D0000"
        }

        connection.sendAsync(message) { (response) ->
            response ?: return@sendAsync println("CallHome failed")
        }
    }

    private fun createTimer(): TimerTask {
        isCallHomeTimerRunning = true
        return Timer().schedule(period, period) {
            callHome()
        }
    }
}