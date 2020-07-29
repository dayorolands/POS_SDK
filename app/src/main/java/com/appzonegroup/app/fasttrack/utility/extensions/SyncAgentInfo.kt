package com.appzonegroup.app.fasttrack.utility.extensions

import android.content.ComponentCallbacks
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRunIO
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.get

suspend fun ComponentCallbacks.syncAgentInfo(): Boolean {
    val creditClubMiddleWareAPI: CreditClubMiddleWareAPI = get()
    val localStorage: LocalStorage = get()
    val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

    val (agent, error) = safeRunIO {
        creditClubMiddleWareAPI.staticService.getAgentInfoByPhoneNumber(
            localStorage.institutionCode,
            localStorage.agentPhone
        )
    }

    if (error != null) return false
    agent ?: return false

    localStorage.agent = agent
    firebaseCrashlytics.setUserId(agent.agentCode ?: "0")
    firebaseCrashlytics.setCustomKey("agent_name", agent.agentName ?: "")
    firebaseCrashlytics.setCustomKey("agent_phone", agent.phoneNumber ?: "")
    firebaseCrashlytics.setCustomKey("terminal_id", agent.terminalID ?: "")

    if (Platform.isPOS) {
        val configHasChanged =
            posConfig.terminalId != agent.terminalID // || posConfig.posModeStr != agent.posMode

        if (configHasChanged) {
//                    posConfig.posModeStr = agent.posMode
            posConfig.terminalId = agent.terminalID ?: ""
            posParameter.reset()
        }
    }

    return true
}