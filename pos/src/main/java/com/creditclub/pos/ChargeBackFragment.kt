package com.creditclub.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.pos.api.ChargeBackService
import com.creditclub.pos.ui.ChargeBack
import com.creditclub.pos.ui.GetPosTransaction
import com.creditclub.pos.ui.LogDispute
import com.creditclub.pos.ui.ResolveDispute
import com.creditclub.ui.theme.CreditClubTheme
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

