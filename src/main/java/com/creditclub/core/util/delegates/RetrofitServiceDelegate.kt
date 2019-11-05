package com.creditclub.core.util.delegates

import retrofit2.Retrofit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 20/10/2019.
 * Appzone Ltd
 */
class RetrofitServiceDelegate<T>(
    private val retrofit: Retrofit,
    private val serviceClass: Class<T>
) {
    private var value: T? = null

    operator fun getValue(obj: Any, property: KProperty<*>): T {
        return value ?: retrofit.create(serviceClass).also { value = it }
    }
}

fun <T : Any> Retrofit.service(serviceClass: KClass<T>): RetrofitServiceDelegate<T> {
    return RetrofitServiceDelegate(this, serviceClass.java)
}