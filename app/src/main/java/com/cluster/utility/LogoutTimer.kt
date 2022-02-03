package com.cluster.utility

import java.util.*
import kotlin.concurrent.schedule

private const val DELAY = 10 * 60 * 1000L

class AppTimer(private val block: TimerTask.() -> Unit) {
    var isRunning = false
        private set
    private var task: TimerTask? = null

    fun start() {
        if (!isRunning) {
            isRunning = true
            task = Timer().schedule(DELAY, block)
        }
    }

    fun stop() {
        isRunning = false
        task?.cancel()
        task = null
    }

    fun restart() {
        stop()
        start()
    }
}