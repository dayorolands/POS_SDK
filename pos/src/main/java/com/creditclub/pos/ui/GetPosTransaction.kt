package com.creditclub.pos.ui

import android.content.ComponentCallbacks
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.model.IntValueType
import com.creditclub.core.util.format
import com.creditclub.core.util.setResult
import com.creditclub.ui.CreditClubAppBar
import com.creditclub.ui.Select
import org.koin.android.ext.android.get
import java.time.Instant
import java.time.temporal.ChronoUnit

private val periodList = listOf(
    IntValueType(0, "Today"),
    IntValueType(1, "Last 7 days"),
    IntValueType(2, "Last 30 days"),
)

@Composable
fun ComponentCallbacks.GetPosTransaction(
    navController: NavHostController,
    popOnSelect: Boolean = true,
    onResult: ((PosTransaction) -> Unit)? = null
) {
    val posTransactionDao by remember { lazy { get<PosDatabase>().posTransactionDao() } }
    val (query, setQuery) = remember { mutableStateOf("") }
    val (period, setPeriod) = remember { mutableStateOf(periodList[1]) }
    val endDate = remember { Instant.now() }
    val startDate: Instant = remember(period) {
        when (period.value) {
            1 -> Instant.now().minus(6, ChronoUnit.DAYS)
            2 -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now()
        }
    }
    val transactions = remember(query, startDate, endDate) {
        posTransactionDao.failed("%${query}%", startDate, endDate)
    }.observeAsState(emptyList())

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
                    onValueChange = setQuery,
                    label = { Text(text = "STAN or RRN") },
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

            items(transactions.value, key = { it.id }) {
                TransactionItem(transaction = it, onClick = {
                    onResult?.invoke(it)
                    navController.setResult(it)
                    if (popOnSelect) navController.popBackStack()
                })
                Divider(startIndent = 16.dp)
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: PosTransaction, onClick: () -> Unit) {
    val amount = remember {
        CurrencyFormatter.format(transaction.amount?.toInt()?.times(100)?.toString())
    }
    val prettyTime = remember { transaction.dateTime?.format("MM/dd/uuuu hh:mm:ss") }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClick = onClick)
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
}