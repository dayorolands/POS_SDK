package com.cluster.pos

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.printer.PosPrinter
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

@Composable
fun rememberPosPrinter(): Lazy<PosPrinter> {
    val localContext = LocalContext.current
    return remember {
        val application = localContext.applicationContext as Application
        application.inject {
            parametersOf(
                localContext,
                application.get<DialogProvider> { parametersOf(localContext) },
            )
        }
    }
}