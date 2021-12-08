package com.cluster.pos.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.models.PosTransaction
import com.cluster.core.model.IntValueType
import com.cluster.core.util.format
import com.cluster.core.util.setResult
import com.cluster.ui.CreditClubAppBar
import com.cluster.ui.Select
import com.cluster.ui.rememberBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

private val periodList = listOf(
    IntValueType(0, "Today"),
    IntValueType(1, "Last 7 days"),
//    IntValueType(2, "Last 30 days"),
)

@Composable
fun GetPosTransaction(
    navController: NavController,
    popOnSelect: Boolean = true,
    onResult: ((PosTransaction) -> Unit)? = null
) {
    val posDatabase: PosDatabase by rememberBean()
    var query by remember { mutableStateOf("") }
    val (period, setPeriod) = remember { mutableStateOf(periodList[1]) }
    val endDate = remember { Instant.now() }
    val startDate: Instant = remember(period) {
        when (period.value) {
            1 -> Instant.now().minus(6, ChronoUnit.DAYS)
//            2 -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now()
        }
    }
    val transactions by produceState<Flow<List<PosTransaction>>>(
        emptyFlow(),
        query,
        startDate,
        endDate
    ) {
        withContext(Dispatchers.IO) {
            value = posDatabase.posTransactionDao().disputable("%${query}%", startDate, endDate)
        }
    }.value.collectAsState(emptyList())

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = "Select POS Transaction",
            onBackPressed = { navController.popBackStack() },
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(text = "STAN or RRN") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
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

            items(transactions, key = { it.id }) {
                TransactionItem(transaction = it, onClick = {
                    onResult?.invoke(it)
                    if (popOnSelect) {
                        navController.setResult(it)
                        navController.popBackStack()
                    }
                })
            }

            item {
                Spacer(modifier = Modifier.size(50.dp))
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: PosTransaction, onClick: () -> Unit) {
    val amount = transaction.amount ?: "NGN0.00"
    val prettyTime = remember { transaction.dateTime?.format("MM/dd/uuuu hh:mm:ss") }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
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
        Divider(startIndent = 16.dp)
    }
}