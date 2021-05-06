package com.creditclub.core

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/27/2019.
 * Appzone Ltd
 */

object AppFunctions {

    private val FUNCTION_MAP = hashMapOf<Int, AppFunction>()

    operator fun get(id: Int) = FUNCTION_MAP[id]

    fun register(id: Int, appFunction: AppFunction) {
        FUNCTION_MAP[id] = appFunction
    }
}