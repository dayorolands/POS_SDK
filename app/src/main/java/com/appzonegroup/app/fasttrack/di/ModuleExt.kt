package com.appzonegroup.app.fasttrack.di

import com.creditclub.analytics.NetworkMetricsInterceptor
import com.creditclub.core.data.HttpClientConfiguration
import com.creditclub.core.data.api.AuthInterceptor
import com.creditclub.core.data.api.RequestFailureInterceptor
import com.creditclub.core.util.debugOnly
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.instance.InstanceFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import java.io.File
import java.util.concurrent.TimeUnit


inline fun <reified T : OkHttpClient> Module.okHttpClient(
    configuration: HttpClientConfiguration,
    qualifier: Qualifier = named(configuration.qualifierName),
): Pair<Module, InstanceFactory<*>> {
    return single(qualifier = qualifier) {
        val cache = Cache(
            directory = File(androidContext().cacheDir, "http_cache.${configuration.hashCode()}"),
            maxSize = CACHE_SIZE,
        )
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .retryOnConnectionFailure(configuration.retryEnabled)
            .writeTimeout(3, TimeUnit.MINUTES)
            .cache(cache)

        builder.addInterceptor(AuthInterceptor(get(), get()))
        if (configuration.traceEnabled) {
            builder.addInterceptor(NetworkMetricsInterceptor(get(), get(), get(), get()))
        }
        builder.addInterceptor(RequestFailureInterceptor())

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }
        return@single builder.build()
    }.bind(OkHttpClient::class)
}