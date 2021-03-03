package com.creditclub.core.util

import androidx.navigation.NavController

fun <T> NavController.getResult(key: String = "result") =
    currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

fun <T> NavController.setResult(result: T, key: String = "result") {
    previousBackStackEntry?.savedStateHandle?.set(key, result)
}