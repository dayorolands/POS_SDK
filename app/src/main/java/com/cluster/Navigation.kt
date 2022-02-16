package com.cluster

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cluster.ui.FundsTransfer
import com.cluster.ui.PendingTransactions
import com.cluster.ui.ReceiptDetails
import com.cluster.conversation.ConversationContent
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.setResult
import com.cluster.pos.printer.ParcelablePrintJob
import com.cluster.screen.PinChange
import com.cluster.screen.SupportCases
import com.cluster.screen.UssdWithdrawal
import com.cluster.screen.subscription.NewSubscriptionScreen
import com.cluster.screen.subscription.SubscriptionHistoryScreen
import com.cluster.screen.subscription.SubscriptionScreen
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.insets.navigationBarsPadding

fun NavGraphBuilder.clusterNavigation(
    navController: NavController,
    dialogProvider: DialogProvider,
    appViewModel: AppViewModel,
) {
    composable(Routes.FundsTransfer) {
        FundsTransfer(
            navController = navController,
            dialogProvider = dialogProvider,
        )
    }
    composable(Routes.PinChange) {
        PinChange(navController = navController)
    }
    composable(Routes.SupportCases) {
        SupportCases(navController = navController)
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
    composable(Routes.PendingTransactions) {
        PendingTransactions(navController = navController)
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
    composable(Routes.Subscription) {
        SubscriptionScreen(navController = navController)
    }
    composable(Routes.SubscriptionHistory) {
        SubscriptionHistoryScreen(navController = navController)
    }
    composable(Routes.NewSubscription) {
        NewSubscriptionScreen(navController = navController)
    }
}

fun NavController.navigateToReceipt(receipt: ParcelablePrintJob, popBackStack: Boolean = true) {
    setResult(receipt, "receipt")
    currentBackStackEntry?.arguments?.putParcelable("receipt", receipt)
    if (popBackStack) popBackStack()
    navigate(Routes.Receipt)
}