package com.cluster.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
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
fun LoanOfferSection(onRequestLoan: () -> Unit) {
    val appViewModel: AppViewModel = viewModel()
    val loan by appViewModel.agentLoan.collectAsState()

    if (loan == null || !loan!!.isEligible) {
        return
    }

    Card(
        modifier = Modifier
            .heightIn(50.dp, 80.dp)
            .fillMaxWidth()
            .padding(6.dp)
            .clickable(onClick = onRequestLoan),
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
                Text(
                    text = "Get a loan of up to ${loan!!.maxAmount.toCurrencyFormat()}",
                    style = MaterialTheme.typography.button,
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                )
                IconButton(onClick = onRequestLoan) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}