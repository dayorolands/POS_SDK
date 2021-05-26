package com.creditclub.core

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/27/2019.
 * Appzone Ltd
 */

data class AppFunction(
    @IdRes val id: Int,
    @StringRes val label: Int,
    @DrawableRes val icon: Int? = null,
)

object AppFunctions {

    private val FUNCTION_MAP = hashMapOf<Int, AppFunction>()

    operator fun get(id: Int) = FUNCTION_MAP[id]

    fun register(id: Int, appFunction: AppFunction) {
        FUNCTION_MAP[id] = appFunction
    }
}