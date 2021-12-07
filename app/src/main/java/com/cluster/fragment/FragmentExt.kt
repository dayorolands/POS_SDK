package com.cluster.fragment

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.cluster.R
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.pos.printer.ParcelablePrintJob

fun CreditClubFragment.navigateToReceipt(
    receipt: ParcelablePrintJob,
    popBackStack: Boolean = true,
) {
    val navController = findNavController()
    if (popBackStack) {
        navController.popBackStack()
    }
    navController.navigate(R.id.action_to_receipt, bundleOf("receipt" to receipt))
}