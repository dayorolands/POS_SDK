package com.creditclub.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.creditclub.core.data.MIDDLEWARE_CLIENT
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.ui.widget.DialogProvider
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

@Composable
inline fun <reified T : Any> rememberRetrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.retrofitService(clientName)
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

@Composable
fun rememberDialogProvider(): Lazy<DialogProvider> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.inject { parametersOf(localContext) }
    }
}