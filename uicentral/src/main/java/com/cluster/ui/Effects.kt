package com.cluster.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.cluster.core.data.MIDDLEWARE_CLIENT
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import org.koin.android.ext.android.inject

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
        lazy { (localContext as CreditClubActivity).dialogProvider }
    }
}

@Composable
fun rememberTransactionReference(): String {
    val localStorage: LocalStorage by rememberBean()
    return rememberSaveable { localStorage.newTransactionReference() }
}