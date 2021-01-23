@file:JvmName("RetrofitServiceDelegate")

package com.creditclub.core.data.api

import android.content.ComponentCallbacks
import android.content.ComponentCallbacks2
import com.creditclub.core.data.CreditClubMiddleWareAPI
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent
import org.koin.core.get

inline fun <reified T> ComponentCallbacks.retrofitService(): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>().retrofit.create(T::class.java) }
}

inline fun <reified T> ComponentCallbacks2.retrofitService(): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>().retrofit.create(T::class.java) }
}

inline fun <reified T> KoinComponent.retrofitService(): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>().retrofit.create(T::class.java) }
}
