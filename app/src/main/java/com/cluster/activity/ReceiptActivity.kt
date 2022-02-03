package com.cluster.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cluster.ui.ReceiptDetails
import com.cluster.Routes
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.printer.ParcelablePrintJob
import com.cluster.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets

class ReceiptActivity : CreditClubActivity() {
    private val receipt: ParcelablePrintJob by lazy { intent.getParcelableExtra("receipt")!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            CreditClubTheme {
                ProvideWindowInsets {
                    NavHost(navController = navController, startDestination = Routes.Receipt) {
                        composable(Routes.Receipt) {
                            ReceiptDetails(
                                navController = navController,
                                onBackPressed = { finish() },
                                printJob = receipt,
                            )
                        }
                    }
                }
            }
        }
    }
}