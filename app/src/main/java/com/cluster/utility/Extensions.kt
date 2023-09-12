package com.cluster.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.fragment.findNavController
import com.cluster.R
import com.cluster.core.data.CoreDatabase
import com.cluster.core.data.model.AppFunctionUsage
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.debug
import com.cluster.core.util.logFunctionUsage
import com.cluster.pos.Platform
import com.cluster.ui.rememberBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FunctionUsageTracker(fid: Int) {
    val coreDatabase: CoreDatabase by rememberBean()
    var usageHasBeenLogged by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fid) {
        if (!usageHasBeenLogged) {
            withContext(Dispatchers.IO) {
                val appFunctionUsageDao = coreDatabase.appFunctionUsageDao()
                val appFunction = appFunctionUsageDao.getFunction(fid)

                val count = if (appFunction == null) {
                    appFunctionUsageDao.insert(AppFunctionUsage(fid))
                    1
                } else {
                    appFunction.usage++
                    appFunctionUsageDao.update(appFunction)

                    appFunction.usage
                }
                usageHasBeenLogged = true
                debug("Usage for function $fid -> $count")
            }
        }
    }
}

fun CreditClubFragment.openPageById(id: Int) {
    when (id) {
        R.id.card_withdrawal_button -> {
            if (Platform.isPOS) {
                ioScope.launch { logFunctionUsage(FunctionIds.CARD_TRANSACTIONS) }

                findNavController().navigate(R.id.action_to_pos_nav_graph)
            } else{
                ioScope.launch { logFunctionUsage(FunctionIds.CARD_TRANSACTIONS) }
                findNavController().navigate(R.id.action_to_pos_nav_graph)
            }
        }

        else -> dialogProvider.showError(
            "This function is not available at the moment. Please look out for it in our next update."
        )
    }
}
