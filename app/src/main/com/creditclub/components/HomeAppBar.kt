package com.creditclub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.appzonegroup.app.fasttrack.fragment.HomeFragmentDirections
import com.creditclub.core.util.safeRunSuspend
import com.google.accompanist.insets.statusBarsHeight
import kotlinx.coroutines.launch

@Composable
fun HomeAppBar(scaffoldState: ScaffoldState, mainNavController: NavController) {
    val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)
    val coroutineScope = rememberCoroutineScope()
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
                Spacer(
                    modifier = Modifier
                        .weight(1f),
                )
                IconButton(
                    onClick = {
                        mainNavController.navigate(HomeFragmentDirections.homeToNotifications())
                    }
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary.copy(0.52f)
                    )
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0f),
        navigationIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    safeRunSuspend {
                        scaffoldState.drawerState.open()
                    }
                }
            }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary.copy(0.52f)
                )
            }
        },
        elevation = 0.dp
    )
}