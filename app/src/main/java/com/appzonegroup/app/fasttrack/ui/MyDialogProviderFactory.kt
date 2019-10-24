package com.appzonegroup.app.fasttrack.ui

import android.content.Context
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.ui.widget.DialogProviderFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 21/10/2019.
 * Appzone Ltd
 */
class MyDialogProviderFactory : DialogProviderFactory {

    override fun create(context: Context): DialogProvider {
        return MyDialogProvider(context)
    }
}