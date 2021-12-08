package com.cluster.core.data

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NullOnEmptyConverterFactory private constructor() : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, Any?>? {

        val delegate: Converter<ResponseBody, Any?> =
            retrofit!!.nextResponseBodyConverter(this, type!!, annotations!!)

        return Converter { body: ResponseBody ->
            if (body.contentLength() == 0L || body.contentLength() == 4L) null else delegate.convert(
                body
            )
        }
    }

    companion object {

        @JvmStatic
        fun create() = NullOnEmptyConverterFactory()
    }
}