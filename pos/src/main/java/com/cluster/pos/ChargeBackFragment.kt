package com.cluster.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.core.data.api.retrofitService
import com.cluster.core.ui.CreditClubFragment
import com.cluster.pos.api.ChargeBackService
import com.cluster.pos.ui.ChargeBack
import com.cluster.pos.ui.GetPosTransaction
import com.cluster.pos.ui.LogDispute
import com.cluster.pos.ui.ResolveDispute
import com.cluster.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets

class ChargeBackFragment : CreditClubFragment() {
    private val chargeBackService: ChargeBackService by retrofitService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        val navController = rememberNavController()
                        NavHost(navController = navController, "chargeBack") {
                            composable("chargeBack") {
                                ChargeBack(
                                    navController = navController,
                                    onBackPressed = { findNavController().popBackStack() },
                                    chargeBackService = chargeBackService,
                                    dialogProvider = dialogProvider,
                                    localStorage = localStorage,
                                )
                            }

                            composable("getTransaction") {
                                GetPosTransaction(navController = navController)
                            }

                            composable("logDispute") {
                                LogDispute(
                                    navController = navController,
                                    dialogProvider = dialogProvider,
                                )
                            }

                            composable("resolveDispute") {
                                ResolveDispute(
                                    navController = navController,
                                    chargeBackService = chargeBackService,
                                    dialogProvider = dialogProvider,
                                    localStorage = localStorage,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

