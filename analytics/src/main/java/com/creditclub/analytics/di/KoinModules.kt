package com.creditclub.analytics.di

import com.creditclub.analytics.ObjectBox
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val boxModule = module {
    single { ObjectBox.init(androidContext()) }
}