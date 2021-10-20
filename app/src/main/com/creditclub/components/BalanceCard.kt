package com.creditclub.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.util.SuspendCallback
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.ui.rememberBean
import com.creditclub.ui.rememberDialogProvider
import com.creditclub.ui.rememberRetrofitService
import kotlinx.coroutines.launch

@Composable
fun BalanceCard() {
    var balance by remember { mutableStateOf(0.0) }
    var availableBalance by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val balanceFormatted = remember(availableBalance, visible) {
        if (visible) availableBalance.toCurrencyFormat()
        else "XXX,XXX.XX"
    }
    val ledgerBalanceFormatted = remember(balance, visible) {
        if (visible) balance.toCurrencyFormat()
        else "XX,XXX.XX"
    }
    val dialogProvider by rememberDialogProvider()
    val localStorage: LocalStorage by rememberBean()
    val staticService: StaticService by rememberRetrofitService()
    val networkErrorMessage = stringResource(R.string.a_network_error_occurred)
    val loadBalance: SuspendCallback = remember {
        loadBalance@{
            val pin = dialogProvider.getPin("Agent PIN") ?: return@loadBalance
            if (pin.length != 4) {
                dialogProvider.showError("Agent PIN must be 4 digits")
                return@loadBalance
            }
            val request = BalanceEnquiryRequest(
                agentPin = pin,
                agentPhoneNumber = localStorage.agentPhone,
                institutionCode = localStorage.institutionCode,
            )

            loading = true
            val (response) = safeRunIO { staticService.balanceEnquiry(request) }
            loading = false

            if (response == null) return@loadBalance
            if (!response.isSussessful) {
                dialogProvider.showError(response.responseMessage ?: networkErrorMessage)
                return@loadBalance
            }

            availableBalance = response.availableBalance
            balance = response.balance
            visible = true
        }
    }
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .heightIn(100.dp, 170.dp)
            .fillMaxWidth()
            .padding(6.dp),
        elevation = 2.dp,
        backgroundColor = colorResource(R.color.colorBalanceCardBg),
        contentColor = MaterialTheme.colors.onSecondary,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = balanceFormatted,
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEDGER BALANCE: $ledgerBalanceFormatted",
                    style = MaterialTheme.typography.button,
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                )
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (visible) visible = false
                            else coroutineScope.launch { loadBalance() }
                        },
                    ) {
                        Icon(
                            imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}