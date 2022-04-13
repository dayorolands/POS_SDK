package com.cluster.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.SmallMenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.pos.Platform
import com.cluster.utility.openPageById

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    fragment: CreditClubFragment,
    institutionConfig: InstitutionConfig,
    composeNavController: NavController,
) {
    val flows = institutionConfig.flows

    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 100.dp)
    ) {
        item {
            SmallMenuButton(
                text = "Deposit",
                icon = painterResource(R.drawable.deposit),
                onClick = { fragment.openPageById(R.id.deposit_button) }
            )
        }
        if (flows.tokenWithdrawal != null) {
            item {
                SmallMenuButton(
                    text = "Token Withdrawal",
                    icon = painterResource(R.drawable.withdraw),
                    onClick = { fragment.openPageById(R.id.token_withdrawal_button) }
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
        if (Platform.isPOS) {
            item {
                SmallMenuButton(
                    text = "Card Transactions",
                    icon = painterResource(R.drawable.withdraw),
                    onClick = { fragment.openPageById(R.id.card_withdrawal_button) }
                )
            }
        }
        if (flows.billPayment != null) {
            item {
                SmallMenuButton(
                    text = "Bills Payment",
                    icon = painterResource(R.drawable.payday_loan),
                    onClick = { fragment.openPageById(R.id.pay_bill_button) }
                )
            }
        }
        if (flows.airtime != null) {
            item {
                SmallMenuButton(
                    text = "Airtime Topup",
                    icon = painterResource(R.drawable.payday_loan),
                    onClick = { fragment.openPageById(R.id.airtime_button) }
                )
            }
        }
        item {
            SmallMenuButton(
                text = "Funds Transfer",
                icon = painterResource(R.drawable.payday_loan),
                onClick = { composeNavController.navigate(Routes.FundsTransfer) }
            )
        }
        if (flows.collectionPayment != null) {
            item {
                SmallMenuButton(
                    text = "IGR Collections",
                    icon = painterResource(R.drawable.payday_loan),
                    onClick = { fragment.openPageById(R.id.collection_payment_button) }
                )
            }
        }
    }
}