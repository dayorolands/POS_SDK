package com.appzonegroup.app.fasttrack

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.appzonegroup.app.fasttrack.ui.FundsTransfer
import com.appzonegroup.app.fasttrack.ui.PendingTransactions
import com.appzonegroup.app.fasttrack.ui.ReceiptDetails
import com.creditclub.Routes
import com.creditclub.conversation.ConversationContent
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.setResult
import com.creditclub.pos.printer.ParcelablePrintJob
import com.creditclub.screen.PinChange
import com.creditclub.screen.SupportCases
import com.creditclub.screen.UssdWithdrawal
import com.creditclub.viewmodel.AppViewModel
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
}

fun NavController.navigateToReceipt(receipt: ParcelablePrintJob, popBackStack: Boolean = true) {
    setResult(receipt, "receipt")
    currentBackStackEntry?.arguments?.putParcelable("receipt", receipt)
    if (popBackStack) popBackStack()
    navigate(Routes.Receipt)
}