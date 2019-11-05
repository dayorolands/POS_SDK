package com.creditclub.core

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 31/08/2019.
 * Appzone Ltd
 */
open class AppFunction(@IdRes val id: Int, @StringRes var label: Int, @DrawableRes var icon: Int? = null)