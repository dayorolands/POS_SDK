package com.appzonegroup.app.fasttrack.fragment
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
import com.creditclub.Routes
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.screen.UssdWithdrawal
import com.creditclub.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets

class ComposeFragment : CreditClubFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainNavController = findNavController()
        val route = requireArguments().getString("route", "transfer")
        return ComposeView(requireContext()).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        val navController = rememberNavController()
                        NavHost(navController = navController, route) {
                            composable(Routes.FundsTransfer) {
                                FundsTransfer(
                                    navController = mainNavController,
                                    dialogProvider = dialogProvider,
                                )
                            }
                            composable(Routes.UssdWithdrawal) {
                                UssdWithdrawal(navController = mainNavController)
                            }
                        }
                    }
                }
            }
        }
    }
}
