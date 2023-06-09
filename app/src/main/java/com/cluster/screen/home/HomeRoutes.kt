package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cluster.Routes
import com.cluster.components.BottomNavScreens
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.getArrayList
import com.cluster.viewmodel.AppViewModel
import com.cluster.viewmodel.ProvideViewModelStoreOwner
import org.koin.androidx.compose.viewModel

fun NavGraphBuilder.homeRoutes(
    institutionConfig: InstitutionConfig,
    homeNavController: NavHostController,
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
    preferences: SharedPreferences
) {
    val returnedList = getArrayList("institution_features", preferences)
    composable(BottomNavScreens.Home.route) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = fragment.requireActivity(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BalanceCard()
                if(returnedList != null) {
                    if(returnedList.contains("AGL")) {
                        LoanOfferSection(
                            onRequestOverdraft = {
                                composeNavController.navigate(Routes.AgentLoanRequest)
                            },
                            onShowHistory = {
                                composeNavController.navigate(Routes.AgentLoanHistory)
                            },
                            onOverdraftQualify = {
                                composeNavController.navigate(Routes.OverdraftQualify)
                            }
                        )
                    }
                }
                HomeCategoryButtons(
                    homeNavController = homeNavController,
                    institutionConfig = institutionConfig,
                    preferences = preferences
                )
            }
        }
    }
    composable(BottomNavScreens.Customer.route) {
        CustomerScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
            preferences = preferences
        )
    }
    composable(BottomNavScreens.Transactions.route) {
        TransactionsScreen(
            fragment = fragment,
            institutionConfig = institutionConfig,
            composeNavController = composeNavController,
            preferences = preferences
        )
    }
    composable(BottomNavScreens.Loans.route) {
        LoansScreen(fragment = fragment)
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