package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.SmallMenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.getArrayList
import com.cluster.pos.Platform
import com.cluster.utility.openPageById

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    fragment: CreditClubFragment,
    institutionConfig: InstitutionConfig,
    composeNavController: NavController,
    preferences: SharedPreferences
) {
    val flows = institutionConfig.flows
    val returnedList = getArrayList("institution_features", preferences)

    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 100.dp)
    ) {
        if (Platform.isPOS) {
            item {
                SmallMenuButton(
                    text = "Card Transactions",
                    icon = painterResource(R.drawable.withdraw),
                    onClick = { fragment.openPageById(R.id.card_withdrawal_button) }
                )
            }
        }
        if (returnedList != null) {
            if (returnedList.contains("DPS")) {
                item {
                    SmallMenuButton(
                        text = "Deposit",
                        icon = painterResource(R.drawable.deposit),
                        onClick = { fragment.openPageById(R.id.deposit_button) }
                    )
                }
            }
            if (returnedList.contains("TWT") || returnedList.contains("IBTW")) {
                item {
                    SmallMenuButton(
                        text = "Token Withdrawal",
                        icon = painterResource(R.drawable.withdraw),
                        //onClick = { fragment.openPageById(R.id.token_withdrawal_button) }
                        onClick = {
                            composeNavController.navigate(Routes.CardlessWithdrawal)
                        }
                    )
                }
            }
            if (flows.ussdWithdrawal != null) {
                item {
                    SmallMenuButton(
                        text = "USSD Withdrawal",
                        icon = painterResource(R.drawable.withdraw),
                        onClick = {
                            composeNavController.navigate(Routes.UssdWithdrawal)
                        }
                    )
                }
            }
            if (returnedList.contains("BPM")) {
                item {
                    SmallMenuButton(
                        text = "Bills Payment",
                        icon = painterResource(R.drawable.payday_loan),
                        onClick = { fragment.openPageById(R.id.pay_bill_button) }
                    )
                }
            }
            if (returnedList.contains("ATP")) {
                item {
                    SmallMenuButton(
                        text = "Airtime Topup",
                        icon = painterResource(R.drawable.payday_loan),
                        onClick = { fragment.openPageById(R.id.airtime_button) }
                    )
                }
            }
            if (returnedList.contains("LFT") || returnedList.contains("IFT")) {
                item {
                    SmallMenuButton(
                        text = "Funds Transfer",
                        icon = painterResource(R.drawable.payday_loan),
                        onClick = { composeNavController.navigate(Routes.FundsTransfer) }
                    )
                }
            }
            if (returnedList.contains("COL")) {
                item {
                    SmallMenuButton(
                        text = "Collections",
                        icon = painterResource(R.drawable.payday_loan),
                        onClick = { fragment.openPageById(R.id.collection_payment_button) }
                    )
                }
            }
        }
        else //onClick = { fragment.openPageById(R.id.token_withdrawal_button) }
        {
            if (!Platform.isPOS && returnedList == null){
                item {
                    Text(
                        text = "No features available for this agent.",
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxSize(),
                        softWrap = true,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}