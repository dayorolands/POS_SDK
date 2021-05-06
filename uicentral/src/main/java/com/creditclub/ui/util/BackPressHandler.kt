package com.creditclub.ui.util

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
fun BackPressHandler(onBackPressed: () -> Unit, enabled: Boolean = true) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    DisposableEffect(dispatcher) { // dispose/relaunch if dispatcher changes
        dispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove() // avoid leaks!
        }
    }
}