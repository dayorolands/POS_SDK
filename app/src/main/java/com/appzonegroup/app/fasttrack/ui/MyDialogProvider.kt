package com.appzonegroup.app.fasttrack.ui

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.Fragment
import com.creditclub.core.ui.CreditClubActivity

class MyDialogProvider(override val context: Context) : DialogProviderImpl {

    override val activity: CreditClubActivity
        get() = when (context) {
            is CreditClubActivity -> context
            is Fragment -> context.activity as CreditClubActivity
            else -> throw IllegalStateException("Dialog provider context must either be a fragment or activity")
        }

    override val progressDialog: Dialog by lazy {
        Dialogs.getProgress(activity, null)
    }
}
