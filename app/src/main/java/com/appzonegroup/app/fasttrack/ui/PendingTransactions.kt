package com.appzonegroup.app.fasttrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.receipt.fundsTransferReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.app.fasttrack.utility.FunctionUsageTracker
import com.creditclub.Routes
import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.api.FundsTransferService
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.model.PendingTransaction_
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.type.TransactionType
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.ui.*
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun PendingTransactions(
    navController: NavController,
) {
    FunctionUsageTracker(fid = FunctionIds.PENDING_TRANSACTIONS)
    val context = LocalContext.current
    val clusterObjectBox: ClusterObjectBox by rememberBean()
    val pendingTransactionsBox: Box<PendingTransaction> = remember {
        clusterObjectBox.boxStore.boxFor()
    }
    val transactions by remember {
        ObjectBoxLiveData(pendingTransactionsBox.query().orderDesc(PendingTransaction_.id).build())
    }.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    val dialogProvider by rememberDialogProvider()
    val fundsTransferService: FundsTransferService by rememberRetrofitService()
    val checkStatus: suspend CoroutineScope.(transaction: PendingTransaction) -> Unit = remember {
        checkStatus@{ transaction ->
            dialogProvider.showProgressBar("Processing")
            val (response, error) = safeRunIO {
                when (transaction.transactionType) {
                    TransactionType.LocalFundsTransfer,
                    TransactionType.FundsTransferCommercialBank -> {
                        val request = defaultJson.decodeFromString(
                            FundsTransferRequest.serializer(),
                            transaction.requestJson,
                        )
                        fundsTransferService.requery(request)
                    }
                    else -> throw IllegalArgumentException(
                        "requery for " +
                                "${transaction.transactionType} not supported"
                    )
                }
            }
            dialogProvider.hideProgressBar()

            if (error != null) {
                dialogProvider.showErrorAndWait(error)
                return@checkStatus
            }
            if (response!!.isPending()) {
                dialogProvider.showErrorAndWait(response.responseMessage ?: "Pending")
                return@checkStatus
            }
            if (response.isFailure()) {
                dialogProvider.showErrorAndWait(response.responseMessage ?: "Failed")
                return@checkStatus
            }

            pendingTransactionsBox.remove(transaction.id)

            val receipt = when (transaction.transactionType) {
                TransactionType.LocalFundsTransfer,
                TransactionType.FundsTransferCommercialBank -> {
                    fundsTransferReceipt(
                        context = context,
                        request = defaultJson.decodeFromString(
                            FundsTransferRequest.serializer(),
                            transaction.requestJson,
                        ),
                        transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm"),
                        isSuccessful = response.isSuccessful,
                        reason = response.responseMessage,
                    )
                }
                else -> throw IllegalArgumentException(
                    "requery for " +
                            "${transaction.transactionType} not supported"
                )
            }
            navController.setResult(receipt, "receipt")
            navController.currentBackStackEntry?.arguments?.putParcelable("receipt", receipt)
            navController.navigate(Routes.Receipt)
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = "Pending Transactions",
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
            items(transactions, key = { it.id }) {
                TransactionItem(transaction = it, onClick = {
                    coroutineScope.launch { checkStatus(it) }
                })
            }

            item {
                Spacer(modifier = Modifier.size(50.dp))
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: PendingTransaction,
    onClick: () -> Unit,
) {
    val amount = remember { transaction.amount.toCurrencyFormat() }
    val prettyTime = remember { transaction.createdAt.format("MM/dd/uuuu hh:mm:ss") }
    val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${transaction.accountName}, ${transaction.accountNumber}",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
                Text(
                    text = "$prettyTime \u2022 ${transaction.transactionType.label}",
                    style = MaterialTheme.typography.caption,
                    color = captionColor,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.subtitle1,
                    color = captionColor,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppTextButton(onClick = onClick) {
                    Text(
                        text = "Check status",
                        color = colorResource(R.color.menuButtonTextColor),
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Divider(startIndent = 16.dp)
    }
}