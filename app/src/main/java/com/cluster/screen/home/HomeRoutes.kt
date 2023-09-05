package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cluster.components.BottomNavScreens
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.viewmodel.ProvideViewModelStoreOwner

fun NavGraphBuilder.homeRoutes(
    institutionConfig: InstitutionConfig,
    homeNavController: NavHostController,
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
    preferences: SharedPreferences
) {
    composable(BottomNavScreens.Home.route) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = fragment.requireActivity(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeCategoryButtons(
                    homeNavController = homeNavController,
                    institutionConfig = institutionConfig
                )
            }
        }
    }
    composable(BottomNavScreens.Transactions.route) {
        TransactionsScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
            composeNavController = composeNavController,
            preferences = preferences
        )
    }
    composable(BottomNavScreens.Profile.route) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = fragment.requireActivity(),
        ) {
            ProfileScreen(
                composeNavController = composeNavController,
                fragment = fragment,
                preferences = preferences
            )
        }
    }
}