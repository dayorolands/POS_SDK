package com.cluster.ui.manager

import android.content.ComponentCallbacks
import android.os.Bundle
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 24/09/2019.
 * Appzone Ltd
 */
abstract class ActivityManager(activity: CreditClubActivity) : DialogProvider by activity.dialogProvider,
    ComponentCallbacks by activity {

    open fun onCreate(savedInstanceState: Bundle?) {}
    open fun render() {}
}