@file:JvmName("RetrofitServiceDelegate")

package com.cluster.core.data.api

import android.content.ComponentCallbacks
import android.content.ComponentCallbacks2
import com.cluster.core.data.CreditClubMiddleWareAPI
import com.cluster.core.data.MIDDLEWARE_CLIENT
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

inline fun <reified T> Scope.getRetrofitService(clientName: String = MIDDLEWARE_CLIENT): T {
    return get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java)
}

inline fun <reified T> ComponentCallbacks.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}

inline fun <reified T> ComponentCallbacks2.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}

inline fun <reified T> KoinComponent.retrofitService(clientName: String = MIDDLEWARE_CLIENT): Lazy<T> {
    return lazy { get<CreditClubMiddleWareAPI>(named(clientName)).retrofit.create(T::class.java) }
}
