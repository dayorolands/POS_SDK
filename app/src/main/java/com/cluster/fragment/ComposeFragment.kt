package com.cluster.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.clusterNavigation
import com.cluster.Routes
import com.cluster.core.ui.CreditClubFragment
import com.cluster.ui.theme.CreditClubTheme
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.insets.ProvideWindowInsets

class ComposeFragment : CreditClubFragment() {
    private val appViewModel: AppViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainNavController = findNavController()
        val route = requireArguments().getString("route", Routes.FundsTransfer)
        return ComposeView(requireContext()).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        val navController = rememberNavController()
                        NavHost(navController = navController, route) {
                            clusterNavigation(
                                navController = mainNavController,
                                dialogProvider = dialogProvider,
                                appViewModel = appViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
