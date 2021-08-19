package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.component.KoinComponent

abstract class BaseWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    protected val firebaseAnalytics get() = FirebaseAnalytics.getInstance(applicationContext)
}