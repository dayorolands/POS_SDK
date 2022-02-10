package com.cluster.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cluster.components.BottomNavScreens
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment

fun NavGraphBuilder.homeRoutes(
    institutionConfig: InstitutionConfig,
    homeNavController: NavHostController,
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
) {
    composable(BottomNavScreens.Home.route) {
        Column(modifier = Modifier.fillMaxSize()) {
            BalanceCard()
            HomeCategoryButtons(
                homeNavController = homeNavController,
                institutionConfig = institutionConfig,
            )
        }
    }
    composable(BottomNavScreens.Agent.route) {
        AgentScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
            composeNavController = composeNavController,
        )
    }
    composable(BottomNavScreens.Customer.route) {
        CustomerScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
        )
    }
    composable(BottomNavScreens.Transactions.route) {
        TransactionsScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
            composeNavController = composeNavController,
        )
    }
    composable(BottomNavScreens.Loans.route) {
        LoansScreen(fragment = fragment)
    }
}