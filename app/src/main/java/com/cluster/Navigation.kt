package com.cluster

import androidx.navigation.NavController
import com.cluster.core.util.setResult
import com.cluster.pos.printer.ParcelablePrintJob

fun NavController.navigateToReceipt(receipt: ParcelablePrintJob, popBackStack: Boolean = true) {
    setResult(receipt, "receipt")
    currentBackStackEntry?.arguments?.putParcelable("receipt", receipt)
    if (popBackStack) {
        popBackStack()
    }
    navigate(Routes.Receipt)
}