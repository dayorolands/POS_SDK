package com.creditclub.analytics

import android.content.Context
import android.util.Log
import com.creditclub.analytics.models.MyObjectBox
import com.creditclub.core.util.debugOnly
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser

/**
 * Singleton to keep BoxStore reference.
 */
object ObjectBox {

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context): BoxStore {
        if (::boxStore.isInitialized && !boxStore.isClosed) {
            return boxStore
        }

        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()

        debugOnly {
            Log.d(
                "CreditClubAnalytics",
                "Using ObjectBox ${BoxStore.getVersion()} (${BoxStore.getVersionNative()})"
            )
            AndroidObjectBrowser(boxStore).start(context.applicationContext)
        }

        return boxStore
    }
}