package com.cluster.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.fragment.NotificationViewModel
import com.google.accompanist.insets.statusBarsHeight

@Composable
fun HomeAppBar(
    mainNavController: NavController,
    title: String,
    navigationIcon: @Composable () -> Unit,
) {
    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)
    val notificationViewModel: NotificationViewModel = viewModel()
    val badgeCount = notificationViewModel.totalNotification.collectAsState()
    var badgeValue = badgeCount.value
    val notifications by notificationViewModel.notificationList.collectAsState()
    // Draw a scrim over the status bar which matches the app bar
    Spacer(
        Modifier
            .background(appBarColor)
            .fillMaxWidth()
            .statusBarsHeight()
    )
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.h5,
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f),
                )
                for (notification in notifications) {
                    if (notification.isRead == true) badgeValue -= 1 else badgeValue = badgeValue
                }
                BadgedBox(
                    modifier = Modifier
                        .padding(15.dp)
                        .clickable { mainNavController.navigate(R.id.home_to_notifications) },
                    badge = {
                        if(badgeValue > 0){
                            Badge{
                                Text(text = badgeValue.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary.copy(0.52f),
                    )
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0f),
        navigationIcon = navigationIcon,
        elevation = 0.dp
    )
}