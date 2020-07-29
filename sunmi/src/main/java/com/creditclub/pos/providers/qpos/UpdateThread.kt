package com.appzonegroup.creditclub.pos.provider.qpos

/**
 * Created by Emmanuel Nosakhare <enosakhare></enosakhare>@appzonegroup.com> on 07/01/2020.
 * Appzone Ltd
 */
internal class UpdateThread(private val posManager: QPosManager) : Thread() {
    private val activity get() = posManager.activity
    private var concelFlag = false
    override fun run() {
        while (!concelFlag) {
            var i = 0
            while (!concelFlag && i < 100) {
                try {
                    sleep(1)
                } catch (e: InterruptedException) { // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                i++
            }
            if (concelFlag) break
            if (posManager.pos == null) return
            val progress = posManager.pos.updateProgress
            if (progress < 100) {
                activity.runOnUiThread {
                    posManager.showProgressBar("$progress%")
                }
                continue
            }
            activity.runOnUiThread { posManager.showSuccess("Done") }
            break
        }
    }

    fun concelSelf() {
        concelFlag = true
    }

}