package com.cluster.screen.subscription

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.CaseLogActivity
import com.cluster.R
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.getMessage
import com.cluster.core.util.safeRunIO
import com.cluster.ui.CreditClubAppBar
import com.cluster.ui.ErrorFeedback
import com.cluster.ui.rememberBean
import com.cluster.ui.rememberRetrofitService
import com.cluster.utility.FunctionIds
import com.cluster.utility.FunctionUsageTracker
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@Composable
fun SubscriptionScreen(navController: NavController) {
    FunctionUsageTracker(FunctionIds.SUPPORT)

    val context = LocalContext.current
    val subscriptionService: SubscriptionService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    val previousSubscriptions by produceState(emptyList<SubscriptionPlan>(), refreshKey) {
        value = viewModel.subscriptionHistory.value
        loading = true
        val (response, error) = safeRunIO {
            subscriptionService.getSubscriptionHistory(
                localStorage.institutionCode!!,
                localStorage.agentPhone!!,
            )
        }
        loading = false
        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        errorMessage = ""
        if (response?.data == null) return@produceState
        value = response.data!!
        viewModel.subscriptionHistory.value = value
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list, fab) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.subscription),
            onBackPressed = { navController.popBackStack() },
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        )
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = { refreshKey = UUID.randomUUID().toString() },
            modifier = Modifier.constrainAs(list) {
                top.linkTo(appBar.bottom)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (errorMessage.isNotBlank()) {
                    item {
                        ErrorFeedback(errorMessage = errorMessage)
                    }
                }

                items(previousSubscriptions, key = { it.id }) {

                }

                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        CaseLogActivity::class.java
                    )
                )
            },
            icon = { Icon(Icons.Filled.Add, "") },
            text = { Text(stringResource(id = R.string.log_case).uppercase(Locale.ROOT)) },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .constrainAs(fab) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(16.dp),
        )
    }
}