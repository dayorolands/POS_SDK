package com.cluster.core.util.delegates

import retrofit2.Retrofit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 20/10/2019.
 * Appzone Ltd
 */
class RetrofitServiceDelegate<T>(
    private val retrofit: Retrofit,
    private val serviceClass: Class<T>
) : Lazy<T> {
    private var service: T? = null
    override fun isInitialized(): Boolean = service != null
    override val value: T
        get() = service ?: retrofit.create(serviceClass).also { service = it }
}

inline fun <reified T : Any> Retrofit.service(): RetrofitServiceDelegate<T> {
    return RetrofitServiceDelegate(this, T::class.java)
}