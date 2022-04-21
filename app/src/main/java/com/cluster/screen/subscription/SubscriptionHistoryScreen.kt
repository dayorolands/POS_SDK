package com.cluster.screen.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.Subscription
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.model.BooleanValueType
import com.cluster.core.model.IntValueType
import com.cluster.core.util.getMessage
import com.cluster.core.util.safeRunIO
import com.cluster.ui.*
import com.cluster.ui.util.LocalDateSaver
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.util.*

@Composable
fun SubscriptionHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val subscriptionService: SubscriptionService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    val statusList = remember {
        listOf(
            BooleanValueType(true, "Active"),
            BooleanValueType(false, "Inactive"),
        )
    }
    val periodList = remember {
        listOf(
            IntValueType(0, "Today"),
            IntValueType(1, "Last 7 days"),
        )
    }

    var status by remember { mutableStateOf(statusList.first()) }
    var period by remember { mutableStateOf(periodList[1]) }
    val endDate = rememberSaveable(saver = LocalDateSaver) { LocalDate.now() }
    val startDate = remember(period) {
        when (period.value) {
            1 -> LocalDate.now().minusDays(6)
            else -> LocalDate.now()
        }
    }
    var refreshKey by remember(status, period) { mutableStateOf(UUID.randomUUID().toString()) }
    val previousSubscriptions by produceState(emptyList<Subscription>(), refreshKey) {
        value = viewModel.subscriptionHistory.value
        loading = true
        val (response, error) = safeRunIO {
            subscriptionService.getSubscriptionHistory(
                institutionCode = localStorage.institutionCode!!,
                agentPhoneNumber = localStorage.agentPhone!!,
                active = status.value,
                from = startDate.toString(),
                to = endDate.toString(),
                startIndex = 0,
                maxSize = 20,
            )
        }
        loading = false
        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        errorMessage = ""
        if (response?.data == null) return@produceState
        value = response.data!!.reports!!
        viewModel.subscriptionHistory.value = value
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.subscription_history),
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
                item {
                    Select(
                        title = "Status",
                        options = statusList,
                        selected = status,
                        onChange = { status = it },
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    )
                }
                item {
                    Select(
                        title = "Period",
                        options = periodList,
                        selected = period,
                        onChange = { period = it },
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        leadingIcon = {
                            Icon(
                                when (period.value) {
                                    1 -> Icons.Outlined.CalendarViewWeek
                                    2 -> Icons.Outlined.CalendarViewMonth
                                    else -> Icons.Outlined.CalendarToday
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(0.52f)
                            )
                        }
                    ) {
                        Icon(
                            when (it.value) {
                                1 -> Icons.Outlined.CalendarViewWeek
                                2 -> Icons.Outlined.CalendarViewMonth
                                else -> Icons.Outlined.CalendarToday
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colors.onSurface.copy(0.52f)
                        )
                        Text(
                            text = it.label,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(start = 10.dp),
                        )
                    }
                }

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
    }
}