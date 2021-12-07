package com.cluster.di

import android.app.Activity
import android.content.Context
import androidx.work.WorkerParameters
import com.cluster.BuildConfig
import com.cluster.R
import com.cluster.app.LocalInstitutionConfig
import com.cluster.ui.CreditClubDialogProvider
import com.cluster.work.*
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.analytics.NetworkMetricsInterceptor
import com.creditclub.core.config.InstitutionConfig
import com.creditclub.core.data.*
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.api.AuthInterceptor
import com.creditclub.core.data.api.RequestFailureInterceptor
import com.creditclub.core.data.api.getRetrofitService
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.receipt.PdfPrinter
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 23/09/2019.
 * Appzone Ltd
 */

const val CACHE_SIZE = 50L * 1024L * 1024L // 50 MB

val dataModule = module {
    single { LocalStorage(androidContext()) }
    single { AppDataStorage.getInstance(androidContext()) }
    single { CoreDatabase.getInstance(androidContext()) }
}

val apiModule = module {
    single(named(MIDDLEWARE_CLIENT)) {
        val cache = Cache(
            directory = File(androidContext().cacheDir, "http_cache"),
            maxSize = CACHE_SIZE,
        )
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .cache(cache)

        builder
            .addInterceptor(AuthInterceptor(get(), get()))
            .addInterceptor(NetworkMetricsInterceptor(get(), get(), get(), get()))
            .addInterceptor(RequestFailureInterceptor())

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return@single builder.build()
    }

    single(named(TRANSACTIONS_CLIENT)) {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .retryOnConnectionFailure(false)
            .writeTimeout(3, TimeUnit.MINUTES)

        builder
            .addInterceptor(AuthInterceptor(get(), get()))
            .addInterceptor(NetworkMetricsInterceptor(get(), get(), get(), get()))
            .addInterceptor(RequestFailureInterceptor())

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return@single builder.build()
    }

    single(named(MIDDLEWARE_CLIENT)) {
        CreditClubMiddleWareAPI(get(named(MIDDLEWARE_CLIENT)), get<AppConfig>().apiHost)
    }

    single(named(TRANSACTIONS_CLIENT)) {
        CreditClubMiddleWareAPI(get(named(TRANSACTIONS_CLIENT)), get<AppConfig>().apiHost)
    }

    single { CreditClubClient(get(named(MIDDLEWARE_CLIENT)), get<AppConfig>().apiHost) }
}

val uiModule = module {
    factory<DialogProvider> { (context: Context) ->
        CreditClubDialogProvider(context)
    }
}

val configModule = module {
    single<InstitutionConfig> { LocalInstitutionConfig.create(androidContext()) }
    single<AppConfig> {
        object : AppConfig {
            override val apiHost = BuildConfig.API_HOST
            override val posNotificationToken = BuildConfig.NOTIFICATION_TOKEN
            override val appName = androidContext().getString(R.string.app_name)
            override val otaUpdateId = run {
                val baseOtaAppName = androidContext().getString(R.string.ota_app_name)
                if (Platform.isPOS) {
                    "${baseOtaAppName}${Platform.posId}"
                } else {
                    baseOtaAppName
                }
            }
            override val versionName = BuildConfig.VERSION_NAME
            override val versionCode = BuildConfig.VERSION_CODE
            override val fileProviderAuthority = "${BuildConfig.APPLICATION_ID}.provider"
        }
    }
}

val sharingModule = module {
    factory<PosPrinter> { (context: Activity, dialogProvider: DialogProvider) ->
        PdfPrinter(context, dialogProvider)
    }
}

val workerModule = module {
    worker { (workerParams: WorkerParameters) ->
        AppUpdateWorker(
            context = androidContext(),
            params = workerParams,
            appDataStorage = get(),
            localStorage = get(),
            versionService = getRetrofitService(),
            appConfig = get(),
        )
    }
    worker { (workerParams: WorkerParameters) ->
        MobileTrackingWorker(
            context = androidContext(),
            params = workerParams,
            localStorage = get(),
            clusterObjectBox = get(),
        )
    }
}

val posWorkerModule = module {
    worker { (workerParams: WorkerParameters) ->
        ReversalWorker(
            context = androidContext(),
            params = workerParams,
        )
    }
    worker { (workerParams: WorkerParameters) ->
        TransactionLogWorker(
            context = androidContext(),
            params = workerParams,
        )
    }
    worker { (workerParams: WorkerParameters) ->
        PosNotificationWorker(
            context = androidContext(),
            params = workerParams,
            localStorage = get(),
            posDatabase = get(),
            appConfig = get(),
            posConfig = get(),
            creditClubMiddleWareAPI = get(named(MIDDLEWARE_CLIENT)),
        )
    }
    worker { (workerParams: WorkerParameters) ->
        IsoRequestLogWorker(
            context = androidContext(),
            params = workerParams,
        )
    }
}