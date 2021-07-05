package com.appzonegroup.creditclub.pos.service

import com.appzonegroup.creditclub.pos.extension.processingCode3
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.pos.PosConfig
import kotlinx.coroutines.*
import org.jpos.iso.ISOMsg
import java.util.*
import kotlin.concurrent.schedule


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/3/2019.
 * Appzone Ltd
 */

class CallHomeService(private val posConfig: PosConfig, private val connection: IsoSocketHelper) {

    private var isCallHomeTimerRunning = false
    private var callHomeTask = createTimer()
    private val mainScope = CoroutineScope(Dispatchers.Main)

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
            mainScope.cancel()
        }
    }

    suspend fun callHome() {
        val isoMsg = ISOMsg().apply {
            mti = "0800"
            processingCode3 = "9D0000"
        }

        withContext(Dispatchers.IO) {
            connection.send(isoMsg)
        }
    }

    private fun createTimer(): TimerTask {
        isCallHomeTimerRunning = true
        val period = 1000L * posConfig.callHome.toLong()
        return Timer().schedule(period, period) {
            mainScope.launch {
                callHome()
            }
        }
    }
}