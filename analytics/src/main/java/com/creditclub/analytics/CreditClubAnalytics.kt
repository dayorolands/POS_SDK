package com.creditclub.analytics

import com.creditclub.analytics.di.boxModule
import org.koin.core.KoinApplication

fun KoinApplication.loadAnalyticsModules() {
    modules(boxModule)
}