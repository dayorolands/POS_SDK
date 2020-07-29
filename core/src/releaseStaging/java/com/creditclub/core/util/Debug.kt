package com.creditclub.core.util

inline fun debugOnly(block: () -> Unit) {
    block()
}