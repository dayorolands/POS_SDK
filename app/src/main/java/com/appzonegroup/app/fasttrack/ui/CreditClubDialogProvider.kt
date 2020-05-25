package com.appzonegroup.app.fasttrack.ui

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.Fragment
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRun

class CreditClubDialogProvider(override val context: Context) : DialogProviderImpl {

    override val activity: CreditClubActivity
        get() = when (context) {
            is CreditClubActivity -> context
            is Fragment -> context.activity as CreditClubActivity
            else -> throw IllegalStateException("Dialog provider context must either be a fragment or activity")
        }

    private var currentProgressDialog: Dialog? = null

    override val progressDialog: Dialog
        get() {
            if (currentProgressDialog == null) {
                currentProgressDialog = Dialogs.getProgress(activity, null)
            }
            return currentProgressDialog!!
        }

    override fun hideProgressBar() {
        if (!activity.isFinishing) activity.runOnUiThread {
            safeRun {
                if (progressDialog.isShowing && !activity.isFinishing) {
                    progressDialog.dismiss()
                }
            }
            currentProgressDialog = null
        } else currentProgressDialog = null
    }
}
