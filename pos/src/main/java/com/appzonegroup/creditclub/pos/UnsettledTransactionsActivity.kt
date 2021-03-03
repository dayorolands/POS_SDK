package com.appzonegroup.creditclub.pos

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.models.view.NotificationViewModel
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.PosConfig
import com.creditclub.pos.api.PosApiService
import com.creditclub.ui.theme.CreditClubTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class UnsettledTransactionsActivity : CreditClubActivity() {
    private val viewModel: NotificationViewModel by viewModels()
    private val config: PosConfig by inject()
    private val posApiService: PosApiService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreditClubTheme {
                ProvideWindowInsets {
                    UnsettledTransactionsContent()
                }
            }
        }
    }

    @Composable
    private fun UnsettledTransactionsContent() {
        val unsettledTransactions = remember { viewModel.notificationDao.allAsync() }
        val transactions = unsettledTransactions.observeAsState()
        LazyColumn(
            modifier = Modifier.background(MaterialTheme.colors.surface),
        ) {
            items(transactions.value ?: emptyList(), key = { it.id }) {
                Column {
                    TransactionItem(transaction = it, onSettle = ::onSettle)
                    Divider(startIndent = 16.dp)
                }
            }
        }
    }

    private fun onSettle(posNotification: PosNotification) {
        mainScope.launch { settleTransaction(posNotification) }
    }

    private suspend fun settleTransaction(notification: PosNotification) {
        dialogProvider.showProgressBar(getString(R.string.settling))
        val (response, error) = safeRunIO {
            posApiService.posCashOutNotification(
                notification,
                "iRestrict ${backendConfig.posNotificationToken}",
                notification.terminalId ?: config.terminalId
            )
        }

        if (error != null) return dialogProvider.showError(error)

        if (!response?.billerReference.isNullOrBlank()) safeRunIO {
            viewModel.deleteNotification(notification)
        } else {
            return dialogProvider.showError(
                response?.message ?: getString(R.string.a_network_error_occurred)
            )
        }
        dialogProvider.hideProgressBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

@Composable
private inline fun TransactionItem(
    transaction: PosNotification,
    crossinline onSettle: (PosNotification) -> Unit
) {
    val amount = remember { CurrencyFormatter.format(transaction.amount?.times(100).toString()) }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${transaction.maskedPAN}, ${transaction.customerName}",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
            Text(
                text = "${transaction.paymentDate} \u2022 ${transaction.reference}",
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
            TextButton(onClick = { onSettle(transaction) }) {
                Text(text = stringResource(R.string.settle))
            }
        }
    }
}

@Preview
@Composable
private fun TransactionItemPreview() {
    TransactionItem(transaction = PosNotification(), onSettle = {})
}