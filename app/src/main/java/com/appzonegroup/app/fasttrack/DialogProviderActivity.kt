package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.app.Dialog
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.creditclub.core.ui.CreditClubActivity


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
@SuppressLint("Registered")
abstract class DialogProviderActivity : CreditClubActivity(), DialogProviderImpl {

    override val activity: CreditClubActivity
        get() = this

    override val progressDialog: Dialog by lazy {
        Dialogs.getProgress(activity, null)
    }
}