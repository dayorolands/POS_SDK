package com.appzonegroup.app.fasttrack

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
abstract class CreditClubUnitTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val okHttpClient by lazy {
        OkHttpClient().newBuilder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level =
                    if (com.creditclub.core.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private val middleware by lazy { CreditClubMiddleWareAPI(okHttpClient) }

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

    fun <T> ioScope(block: suspend CoroutineScope.() -> T) = runBlocking {
        launch(Dispatchers.IO) {
            block()
        }
    }
}