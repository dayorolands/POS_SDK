package com.cluster.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cluster.core.data.model.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ActiveSubscription(
    subscriptionPlanStateFlow: MutableStateFlow<SubscriptionPlan?>,
    openSubscription: () -> Unit,
) {
    val subscriptionPlan by subscriptionPlanStateFlow.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = subscriptionPlan?.name ?: "Not subscribed",
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp),
            softWrap = true,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
        )
        TextButton(
            onClick = openSubscription,
        ) {
            Text(
                text = "Manage Subscription",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.primary,
            )
        }
    }
}