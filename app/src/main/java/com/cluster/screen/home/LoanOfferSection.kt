package com.cluster.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cluster.R
import com.cluster.core.util.toCurrencyFormat
import com.cluster.viewmodel.AppViewModel

@Composable
fun LoanOfferSection(
    onRequestOverdraft: () -> Unit,
    onShowHistory: () -> Unit,
    onOverdraftQualify: () -> Unit
) {
    val appViewModel: AppViewModel = viewModel()
    val loan by appViewModel.agentLoan.collectAsState()
    val isEligible = loan?.isEligible == true

    Card(
        modifier = Modifier
            .heightIn(50.dp, 80.dp)
            .fillMaxWidth()
            .padding(6.dp)
            .clickable(onClick = if (isEligible) onRequestOverdraft else if(!isEligible) onOverdraftQualify else onShowHistory),
        elevation = 2.dp,
        backgroundColor = colorResource(R.color.colorBalanceCardBg),
        contentColor = MaterialTheme.colors.onSecondary,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEligible) {
                    Text(
                        //text = "Get a loan of up to ${loan!!.maxAmount.toCurrencyFormat()}",
                        text = "Request an Overdraft",
                        style = MaterialTheme.typography.button,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                    )
                } else if(!isEligible){
                    Text(
                        text = "Request an Overdraft",
                        style = MaterialTheme.typography.button,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                    )
                } else {
                    Text(
                        text = "View your loan history",
                        style = MaterialTheme.typography.button,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                    )
                }
                IconButton(onClick = onShowHistory) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null
                    )
                }
                if (isEligible) {
                    IconButton(onClick = onRequestOverdraft) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                if(!isEligible) {
                    IconButton(onClick = onOverdraftQualify) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}