package com.cluster.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun ProvideViewModelStoreOwner(
    viewModelStoreOwner: ViewModelStoreOwner,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        content = content,
    )
}