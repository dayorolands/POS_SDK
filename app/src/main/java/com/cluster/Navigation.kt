package com.cluster

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cluster.conversation.ConversationContent
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.setResult
import com.cluster.pos.printer.ParcelablePrintJob
import com.cluster.screen.*
import com.cluster.screen.cardlesswithdrawal.CardlessWithdrawal
import com.cluster.screen.cardlesswithdrawal.USSDTokenScreen
import com.cluster.screen.loan.AgentLoanHistoryScreen
import com.cluster.screen.loan.AgentLoanRequestScreen
import com.cluster.screen.loan.OverdraftQualifyScreen
import com.cluster.screen.subscription.ChooseSubscriptionScreen
import com.cluster.screen.subscription.SubscriptionHistoryScreen
import com.cluster.screen.subscription.SubscriptionScreen
import com.cluster.viewmodel.AppViewModel
import com.cluster.viewmodel.ProvideViewModelStoreOwner
import com.google.accompanist.insets.navigationBarsPadding

fun NavGraphBuilder.clusterNavigation(
    navController: NavController,
    dialogProvider: DialogProvider,
    appViewModel: AppViewModel,
    viewModelStoreOwner: ViewModelStoreOwner,
    context: Context,
    fragment: CreditClubFragment,
    preferences: SharedPreferences
) {
    composable(Routes.FundsTransfer) {
        FundsTransfer(
            navController = navController,
            dialogProvider = dialogProvider,
            preferences = preferences
        )
    }

    composable(Routes.CardlessWithdrawal) {
        CardlessWithdrawal(
            navController = navController,
            fragment = fragment,
            preferences = preferences
        )
    }
    
    composable(Routes.USSDTokenWithdrawal){
        USSDTokenScreen(
            navController = navController,
            dialogProvider = dialogProvider
        )
    }

    composable(Routes.PayWithTransfer){
        PayWithTransfer(
            navController = navController,
            dialogProvider = dialogProvider
        )
    }
    
    composable(Routes.PinChange) {
        PinChange(navController = navController)
    }
    composable(Routes.ChangePassword) {
        ChangePasswordScreen(navController = navController)
    }
    composable(Routes.SupportCases) {
        SupportCases(navController = navController)
    }
    composable(Routes.PendingTransactions) {
        PendingTransactions(navController = navController)
    }
    composable(Routes.SupportConversation) { backStackEntry ->
        val fcmToken by appViewModel.fcmToken
        val arguments = backStackEntry.arguments!!
        val reference = arguments.getString("reference")!!
        val title = arguments.getString("title")

        ConversationContent(
            title = title ?: "Case",
            fcmToken = fcmToken,
            reference = reference,
            onNavIconPressed = { navController.popBackStack() },
            navController = navController,
            // Add padding so that we are inset from any left/right navigation bars
            // (usually shown when in landscape orientation)
            modifier = Modifier.navigationBarsPadding(bottom = false),
            navigateToProfile = {},
        )
    }
    composable(Routes.UssdWithdrawal) {
        UssdWithdrawal(navController = navController)
    }

    composable(Routes.Receipt) {
        val printJob: ParcelablePrintJob? = navController
            .previousBackStackEntry
            ?.arguments
            ?.getParcelable("receipt")
        ReceiptDetails(
            navController = navController,
            printJob = printJob ?: return@composable,
        )
    }
    composable(Routes.AgentLoanRequest) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            AgentLoanRequestScreen(
                navController = navController,
                context = context
            )
        }
    }
    composable(Routes.AgentLoanHistory) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            AgentLoanHistoryScreen(navController = navController)
        }
    }
    composable(Routes.OverdraftQualify){
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner
        ) {
            OverdraftQualifyScreen(navController = navController, context = context)
        }
    }
}

fun NavGraphBuilder.subscriptionNavigation(
    navController: NavController,
    viewModelStoreOwner: ViewModelStoreOwner,
    context: Context
) {
    composable(Routes.Subscription) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            SubscriptionScreen(navController = navController, context = context)
        }
    }
    composable(Routes.SubscriptionHistory) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            SubscriptionHistoryScreen(navController = navController)
        }
    }
    composable(Routes.NewSubscription) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            ChooseSubscriptionScreen(
                navController = navController,
                isUpgrade = false,
                isChangeSubscription = false,
                context = context
            )
        }
    }
    composable(Routes.UpgradeSubscription) {
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            ChooseSubscriptionScreen(
                navController = navController,
                isUpgrade = true,
                isChangeSubscription = false,
                context = context
            )
        }
    }

    composable(Routes.ChangeSubscription){
        ProvideViewModelStoreOwner(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            ChooseSubscriptionScreen(
                navController = navController,
                isUpgrade = false,
                isChangeSubscription = true,
                context = context
            )
        }
    }
}

fun NavController.navigateToReceipt(receipt: ParcelablePrintJob, popBackStack: Boolean = true) {
    setResult(receipt, "receipt")
    currentBackStackEntry?.arguments?.putParcelable("receipt", receipt)
    if (popBackStack) {
        popBackStack()
    }
    navigate(Routes.Receipt)
}