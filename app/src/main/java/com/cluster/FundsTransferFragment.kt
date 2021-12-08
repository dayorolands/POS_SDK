package com.cluster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.ui.FundsTransfer
import com.cluster.utility.FunctionIds
import com.cluster.core.ui.CreditClubFragment
import com.cluster.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets

class FundsTransferFragment : CreditClubFragment() {
    override val functionId = FunctionIds.FUNDS_TRANSFER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainNavController = findNavController()
        return ComposeView(inflater.context).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        val navController = rememberNavController()
                        NavHost(navController = navController, Routes.FundsTransfer) {
                            composable(Routes.FundsTransfer) {
                                FundsTransfer(
                                    navController = mainNavController,
                                    dialogProvider = dialogProvider,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
