package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.ui.FundsTransfer
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.ui.theme.CreditClubTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

class FundsTransferFragment : CreditClubFragment() {
    override val functionId = FunctionIds.FUNDS_TRANSFER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainNavController = findNavController()
        return ComposeView(requireContext()).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        val navController = rememberNavController()
                        NavHost(navController = navController, "fundsTransfer") {
                            composable("fundsTransfer") {
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
