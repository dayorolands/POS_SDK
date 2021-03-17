package com.creditclub.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.creditclub.core.data.api.retrofitService
import org.koin.android.ext.android.inject

@Composable
inline fun <reified T : Any> rememberRetrofitService(): Lazy<T> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.retrofitService()
    }
}

@Composable
inline fun <reified T : Any> rememberBean(): Lazy<T> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.inject()
    }
}