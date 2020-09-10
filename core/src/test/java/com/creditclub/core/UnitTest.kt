package com.creditclub.core

import com.creditclub.core.data.CreditClubMiddleWareAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/09/2019.
 * Appzone Ltd
 */
abstract class UnitTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val okHttpClient by lazy {
        OkHttpClient().newBuilder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    internal val middleWareAPI by lazy { CreditClubMiddleWareAPI(okHttpClient, "https://staging.mybankone.com") }

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    fun <T> mainScope(block: suspend CoroutineScope.() -> T) = runBlocking {
        launch(Dispatchers.Main) {
            block()
        }
    }
}