@file:JvmName("RetrofitServiceDelegate")

package com.creditclub.core.data.api

import android.content.ComponentCallbacks
import android.content.ComponentCallbacks2
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.MIDDLEWARE_CLIENT
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

inline fun <reified T> ComponentCallbacks.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}

inline fun <reified T> ComponentCallbacks2.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}

inline fun <reified T> KoinComponent.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}
