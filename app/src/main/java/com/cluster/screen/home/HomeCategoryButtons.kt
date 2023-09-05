package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cluster.R
import com.cluster.components.BottomNavScreens
import com.cluster.components.SmallMenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.util.delegates.getArrayList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCategoryButtons(
    homeNavController: NavHostController,
    institutionConfig: InstitutionConfig
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 100.dp)
    ) {
        item {
            SmallMenuButton(
                text = "Transactions",
                icon = painterResource(R.drawable.deposit),
                onClick = {
                    homeNavController.navigate(BottomNavScreens.Transactions.route) {
                        launchSingleTop = true
                        popUpTo(BottomNavScreens.Home.route) {
                            inclusive = false
                        }
                    }
                },
            )
        }
    }
}