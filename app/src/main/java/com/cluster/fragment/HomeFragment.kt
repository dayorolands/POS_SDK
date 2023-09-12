package com.cluster.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.Routes
import com.cluster.conversation.LocalBackPressedDispatcher
import com.cluster.core.ui.CreditClubFragment
import com.cluster.screen.home.HomeScreen
import com.cluster.ui.theme.CreditClubTheme
import com.cluster.viewmodel.AppViewModel
import com.cluster.viewmodel.ProvideViewModelStoreOwner
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.ProvideWindowInsets

class HomeFragment : CreditClubFragment() {
    private val appViewModel: AppViewModel by activityViewModels()
    private val preferences by lazy { context?.getSharedPreferences("JSON_STORAGE", 0) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @OptIn(ExperimentalAnimatedInsets::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fragmentNavController = findNavController()
        return ComposeView(inflater.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val viewModelStoreOwner: ViewModelStoreOwner = requireActivity()

            setContent {
                val composeNavController = rememberNavController()
                CreditClubTheme {
                    ProvideWindowInsets {
                        CompositionLocalProvider(
                            LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher,
                        ) {
                            NavHost(
                                navController = composeNavController,
                                startDestination = Routes.Home,
                            ) {
                                composable(Routes.Home) {
                                    ProvideViewModelStoreOwner(
                                        viewModelStoreOwner = viewModelStoreOwner,
                                    ) {
                                        HomeScreen(
                                            mainNavController = fragmentNavController,
                                            composeNavController = composeNavController,
                                            fragment = this@HomeFragment,
                                            preferences = preferences!!
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

