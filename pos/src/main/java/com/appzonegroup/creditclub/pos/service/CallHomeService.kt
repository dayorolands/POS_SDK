package com.appzonegroup.creditclub.pos.service

import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.models.messaging.NetworkManagement
import com.appzonegroup.creditclub.pos.models.messaging.isoMsg
import java.util.*
import kotlin.concurrent.schedule


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/3/2019.
 * Appzone Ltd
 */

class CallHomeService(
    private val config: ConfigService,
    parameters: ParameterService
) {
    private val period get() = 1000L * config.callHome.toLong()
    private var isCallHomeTimerRunning = false
    private val connection by lazy { IsoSocketHelper(config, parameters) }
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


    companion object {
        private var INSTANCE: CallHomeService? = null

        fun getInstance(config: ConfigService, parameters: ParameterService): CallHomeService {
            if (INSTANCE == null) INSTANCE = CallHomeService(config, parameters)
            return INSTANCE as CallHomeService
        }
    }
}