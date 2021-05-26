package com.appzonegroup.app.fasttrack.fragment

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.pos.printer.ParcelablePrintJob

fun CreditClubFragment.navigateToReceipt(receipt: ParcelablePrintJob) {
    val navController = findNavController()
    navController.popBackStack()
    navController.navigate(R.id.action_to_receipt, bundleOf("receipt" to receipt))
}