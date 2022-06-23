package com.cluster.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.cluster.viewmodel.AppViewModel
import com.cluster.viewmodel.ProvideViewModelStoreOwner
import org.koin.androidx.compose.viewModel

fun NavGraphBuilder.homeRoutes(
    institutionConfig: InstitutionConfig,
    homeNavController: NavHostController,
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
    viewModel: AppViewModel,
) {
    composable(BottomNavScreens.Home.route) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = fragment.requireActivity(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BalanceCard()
                if(institutionConfig.categories.loans) {
                    LoanOfferSection(
                        onRequestLoan = {
                            composeNavController.navigate(Routes.AgentLoanRequest)
                        },
                        onShowHistory = {
                            composeNavController.navigate(Routes.AgentLoanHistory)
                        }
                    )
                }
                HomeCategoryButtons(
                    homeNavController = homeNavController,
                    institutionConfig = institutionConfig,
                )
            }
        }
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
    composable(BottomNavScreens.Profile.route) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = fragment.requireActivity(),
        ) {
            ProfileScreen(
                composeNavController = composeNavController,
                fragment = fragment,
            )
        }
    }

//    @Composable
//    fun alertDialog(){
//        val context = LocalContext.current
//        val openDialog = remember{ mutableStateOf(true) }
//
//        if(openDialog.value){
//            AlertDialog(
//                onDismissRequest = { openDialog.value = false },
//                title = {Text(text= "AlertDialog", color = MaterialTheme.colors.onSurface)},
//                text = { Text(text = "Your validity period is ")}
//            )
//        }
//    }
}