package com.cluster.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cluster.viewmodel.AppViewModel

@Composable
fun ActiveSubscription(
    openSubscription: () -> Unit,
) {
    val viewModel: AppViewModel = viewModel()
    val subscription by viewModel.activeSubscription.collectAsState()
    val daysToExpire by viewModel.planDaysToExpire.collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = subscription?.plan?.name ?: "Not subscribed",
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp),
            softWrap = true,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = openSubscription,
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .border(
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colors.primary
                    ),
                    RoundedCornerShape(15.dp),
                )
        ) {
            Text(
                text = "Manage Subscription",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
            )
            if (daysToExpire != null && daysToExpire!! < 5) {
                Text(
                    text = "(Expiring soon)",
                    modifier = Modifier
                        .padding(start = 5.dp),
                    softWrap = true,
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.error,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}