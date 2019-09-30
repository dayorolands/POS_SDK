package com.appzonegroup.app.fasttrack.utility

import android.view.View

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/6/2019.
 * Appzone Ltd
 */
interface ButtonHandler : View.OnClickListener {
    override fun onClick(view: View)
}