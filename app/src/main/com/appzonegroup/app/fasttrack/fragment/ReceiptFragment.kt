package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.ui.ReceiptDetails
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.pos.printer.ParcelablePrintJob
import com.creditclub.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets

class ReceiptFragment : CreditClubFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
        return ComposeView(inflater.context).apply {
            val receipt = requireArguments().getParcelable<ParcelablePrintJob>("receipt")!!
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        ReceiptDetails(
                            navController = navController,
                            printJob = receipt,
                        )
                    }
                }
            }
        }
    }
}
