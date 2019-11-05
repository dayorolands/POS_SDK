package com.creditclub.ui.manager

import androidx.databinding.ViewDataBinding
import com.creditclub.core.ui.CreditClubActivity


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 24/09/2019.
 * Appzone Ltd
 */
abstract class DataBindingActivityManager<T : ViewDataBinding>(activity: CreditClubActivity) :
    ActivityManager(activity) {

    abstract val binding: T
}