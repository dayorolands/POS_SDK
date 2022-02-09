package com.creditclub.core.util

import android.util.Log

inline fun debugOnly(block: () -> Unit) {
    block()
}

inline fun debug(message: String) = debugOnly {
    Log.d("debug", message)
}
