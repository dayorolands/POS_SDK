package com.creditclub.pos.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.models.DisputedPosTransaction
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.model.IntValueType
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.format
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.api.ChargeBackService
import com.creditclub.ui.CreditClubAppBar
import com.creditclub.ui.Select
import com.creditclub.ui.util.LocalDateSaver
import java.time.LocalDate

private val statusList = listOf(
    IntValueType(0, "Not processed"),
    IntValueType(1, "Pending"),
    IntValueType(2, "Reversed"),
    IntValueType(3, "Approved"),
)

private val periodList = listOf(
    IntValueType(0, "Today"),
    IntValueType(1, "Last 7 days"),
//    IntValueType(2, "Last 30 days"),
)

@Composable
fun ChargeBack(
    navController: NavHostController,
    onBackPressed: () -> Unit = { navController.popBackStack() },
    chargeBackService: ChargeBackService,
    localStorage: LocalStorage,
    dialogProvider: DialogProvider,
) {
    val (disputeStatus, setDisputeStatus) = remember { mutableStateOf(statusList.first()) }
    val (period, setPeriod) = remember { mutableStateOf(periodList[1]) }
    val endDate = rememberSaveable(saver = LocalDateSaver) { LocalDate.now() }
    val startDate = remember(period) {
        when (period.value) {
            1 -> LocalDate.now().minusDays(6)
//            2 -> LocalDate.now().minusDays(30)
            else -> LocalDate.now()
        }
    }
    val (loading, setLoading) = remember { mutableStateOf(false) }
    val transactions =
        produceState(emptyList<DisputedPosTransaction>(), startDate, endDate, disputeStatus) {
            setLoading(true)
            val (response, error) = safeRunIO {
                chargeBackService.getDisputeTransactions(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    disputeStatus.value,
                    startDate.toString(),
                    endDate.toString(),
                    0,
                    20,
                )
            }
            setLoading(false)
            if (error != null) {
                dialogProvider.showError(error)
                value = emptyList()
                return@produceState
            }
            value = response?.data ?: emptyList()
        }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list, fab, resolveDisputeFab) = createRefs()

        CreditClubAppBar(
            title = "Chargeback Disputes",
            onBackPressed = onBackPressed,
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        )
        LazyColumn(
            modifier = Modifier.constrainAs(list) {
                top.linkTo(appBar.bottom)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        ) {
            item {
                Select(
                    title = "Status",
                    options = statusList,
                    selected = disputeStatus,
                    onChange = setDisputeStatus,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                )
            }
            item {
                Select(
                    title = "Period",
                    options = periodList,
                    selected = period,
                    onChange = setPeriod,
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

            if (loading) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(bottom = 10.dp))
                        Text(
                            text = "Loading Disputes",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface.copy(0.52f),
                        )
                    }
                }
            } else {
                items(transactions.value, key = { it.id }) {
                    DisputeTransactionItem(transaction = it)
                    Divider(startIndent = 16.dp)
                }
            }

        }
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate("logDispute") {
                    anim {
                        popEnter = R.anim.fade_in
                        popExit = R.anim.fade_out
                        enter = R.anim.fade_in
                        exit = R.anim.fade_out
                    }
                }
            },
//            icon = { Icon(Icons.Filled.CreditCard, "") },
            text = { Text("Log Dispute") },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .constrainAs(fab) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .padding(16.dp),
        )

        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate("resolveDispute") {
                    anim {
                        popEnter = R.anim.fade_in
                        popExit = R.anim.fade_out
                        enter = R.anim.fade_in
                        exit = R.anim.fade_out
                    }
                }
            },
//            icon = { Icon(Icons.Filled.CreditCard, "") },
            text = { Text("Resolve Dispute") },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .constrainAs(resolveDisputeFab) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .padding(16.dp),
        )
    }
}

@Composable
private fun DisputeTransactionItem(transaction: DisputedPosTransaction) {
    val amount = remember { transaction.amount.toCurrencyFormat() }
    val prettyTime = remember { transaction.transactionDate?.format("MM/dd/uuuu hh:mm:ss") }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${transaction.pan}, ${transaction.cardHolder}",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
            Text(
                text = "$prettyTime \u2022 ${transaction.retrievalReferenceNumber}",
                style = MaterialTheme.typography.caption,
                color = captionColor,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.subtitle1,
                color = captionColor,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}