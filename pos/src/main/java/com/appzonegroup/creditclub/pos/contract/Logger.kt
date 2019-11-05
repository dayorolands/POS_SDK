package com.appzonegroup.creditclub.pos.contract

import android.util.Log
import com.appzonegroup.creditclub.pos.BuildConfig


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
interface Logger {
    val tag: String

    fun log(text: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, text)
        }
    }

    fun logError(text: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, text)
        }
    }
}