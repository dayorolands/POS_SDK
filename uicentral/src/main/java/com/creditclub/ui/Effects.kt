package com.creditclub.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.ui.widget.DialogProvider
import org.koin.android.ext.android.inject
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier

@Composable
inline fun <reified T : Any> rememberRetrofitService(): Lazy<T> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.retrofitService()
    }
}

@Composable
inline fun <reified T : Any> rememberBean(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.inject(qualifier, parameters)
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