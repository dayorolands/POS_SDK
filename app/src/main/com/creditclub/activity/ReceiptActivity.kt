package com.creditclub.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appzonegroup.app.fasttrack.ui.ReceiptDetails
import com.creditclub.Routes
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.pos.printer.ParcelablePrintJob
import com.creditclub.ui.theme.CreditClubTheme
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