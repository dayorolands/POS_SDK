package com.creditclub.core.data

import android.content.Context
import android.util.Log
import com.creditclub.core.data.model.MyObjectBox
import com.creditclub.core.util.debugOnly
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class ClusterObjectBox(val boxStore: BoxStore)

private var boxStore: BoxStore? = null

val clusterObjectBoxModule = module {
    single<ClusterObjectBox> {
        if (boxStore != null) return@single ClusterObjectBox(boxStore!!)
        val context = androidContext()
        boxStore = MyObjectBox
            .builder()
            .androidContext(context.applicationContext)
            .directory(context.getDir("cluster_core", Context.MODE_PRIVATE))
            .build()

        debugOnly {
            Log.d(
                "ClusterCore",
                "Using ObjectBox ${BoxStore.getVersion()} (${BoxStore.getVersionNative()})"
            )
            AndroidObjectBrowser(boxStore).start(context.applicationContext)
        }

        ClusterObjectBox(boxStore!!)
    }
}