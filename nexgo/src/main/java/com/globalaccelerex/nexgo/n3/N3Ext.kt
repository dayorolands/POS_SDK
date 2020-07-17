package com.globalaccelerex.nexgo.n3

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun ComponentActivity.getActivityResult(intent: Intent): ActivityResult =
    suspendCoroutine { continuation ->
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            continuation.resume(result)
        }.launch(intent)
    }

suspend fun Fragment.getActivityResult(intent: Intent): ActivityResult =
    suspendCoroutine { continuation ->
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            continuation.resume(result)
        }.launch(intent)
    }

