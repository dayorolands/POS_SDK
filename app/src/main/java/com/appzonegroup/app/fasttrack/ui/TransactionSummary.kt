package com.appzonegroup.app.fasttrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.model.AgentFee
import com.creditclub.core.data.response.GenericResponse
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.ui.AppButton
import com.creditclub.ui.ErrorMessage
import com.creditclub.ui.Loading
import com.creditclub.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DataItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .fillMaxWidth()
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Divider()
    }
}


@Composable
fun TransactionSummary(
    amount: Double,
    onProceed: suspend CoroutineScope.() -> Unit,
    fetchFeeAgent: suspend CoroutineScope.() -> GenericResponse<AgentFee>?,
    accountInfo: AccountInfo,
) {
    val formattedAmount = remember { amount.toCurrencyFormat() }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val agentFee: AgentFee? by produceState<AgentFee?>(null) {
        errorMessage = ""
        val (response, error) = safeRunIO {
            fetchFeeAgent()
        }
        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        if (response == null) {
            return@produceState
        }
        value = response.data
    }
    val formattedAgentFee = remember(agentFee) { agentFee?.totalFee?.toCurrencyFormat() }

    CreditClubTheme {
        ProvideWindowInsets {
            Column(
                modifier = Modifier
                    .background(colorResource(R.color.menuBackground))
                    .fillMaxSize(),
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item(key = "account-name") {
                        DataItem(
                            label = "Account Name",
                            value = accountInfo.accountName
                        )
                    }
                    item(key = "account-number") {
                        DataItem(
                            label = "Account Number",
                            value = accountInfo.number
                        )
                    }
                    item(key = "confirm-amount") {
                        DataItem(label = "Amount", value = formattedAmount)
                    }
                    item(key = "agent-fee") {
                        if (agentFee == null) {
                            Loading(message = "Loading Agent Fee")
                        } else {
                            DataItem(label = "Agent Fee", value = formattedAgentFee ?: "NA")
                        }
                    }

                    item(key = "error") {
                        ErrorMessage(errorMessage)
                    }
                }
                AppButton(
                    onClick = { coroutineScope.launch { onProceed() } }
                ) {
                    Text(text = stringResource(R.string.continue_))
                }
            }
        }
    }
}