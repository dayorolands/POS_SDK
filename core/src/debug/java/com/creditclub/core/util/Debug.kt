package com.creditclub.core.util

inline fun debugOnly(crossinline block: () -> Unit) = block()