package com.cluster.screen.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
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
import com.cluster.core.data.api.AgentLoansService
import com.cluster.core.data.model.AgentLoanRecord
import com.cluster.core.data.model.AgentLoanSearchRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.model.IntValueType
import com.cluster.core.util.format
import com.cluster.core.util.getMessage
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.ui.*
import com.cluster.ui.util.LocalDateSaver
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.util.*

@Composable
fun AgentLoanHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val agentLoansService: AgentLoansService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    val periodList = remember {
        listOf(
            IntValueType(0, "Today"),
            IntValueType(1, "Last 7 days"),
        )
    }

    var period by remember { mutableStateOf(periodList[1]) }
    val endDate = rememberSaveable(saver = LocalDateSaver) { LocalDate.now() }
    val startDate = remember(period) {
        when (period.value) {
            1 -> LocalDate.now().minusDays(6)
            else -> LocalDate.now()
        }
    }
    var refreshKey by remember(period) { mutableStateOf(UUID.randomUUID().toString()) }
    val previousSubscriptions by produceState(emptyList<AgentLoanRecord>(), refreshKey) {
        value = viewModel.agentLoanHistory.value
        loading = true
        val searchRequest = AgentLoanSearchRequest(
            institutionCode = localStorage.institutionCode!!,
            phoneNumber = localStorage.agentPhone!!,
            fromDate = startDate.toString(),
            toDate = endDate.toString(),
            startIndex = 0,
            maxSize = 20,
        )
        val (response, error) = safeRunIO {
            agentLoansService.search(searchRequest)
        }
        loading = false
        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        errorMessage = ""
        if (response?.reports == null) {
            return@produceState
        }
        value = response.reports!!
        viewModel.agentLoanHistory.value = value
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.overdraft_history),
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

                items(previousSubscriptions, key = { it.disbursementDate.toString() }) {
                    AgentLoanItem(item = it)
                }

                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun AgentLoanItem(item: AgentLoanRecord) {
    val amount = remember { item.requestedAmount.toCurrencyFormat() }
    val fee = remember { item.fee.toCurrencyFormat() }
    val disbursementDate = remember { item.disbursementDate?.format("MM/dd/uuuu hh:mm:ss") }
    val repaymentDate = remember { item.repaymentDate?.format("MM/dd/uuuu hh:mm:ss") }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row {
            Text(
                text = amount,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .weight(1f)
            )
            Text(
                text = "Fee: $fee",
                style = MaterialTheme.typography.subtitle1,
                color = captionColor,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Text(
            text = "Disbursed at $disbursementDate \u2022 To be repaid at $repaymentDate",
            style = MaterialTheme.typography.caption,
            color = captionColor,
        )
    }
    Divider(startIndent = 16.dp)
}