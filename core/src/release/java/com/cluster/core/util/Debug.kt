package com.cluster.core.util

inline fun debugOnly(crossinline ignored: () -> Unit) {}

inline fun debug(message: String) {}
