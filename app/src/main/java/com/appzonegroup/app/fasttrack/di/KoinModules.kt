package com.appzonegroup.app.fasttrack.di

import android.app.Activity
import android.content.Context
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.app.LocalInstitutionConfig
import com.appzonegroup.app.fasttrack.ui.CreditClubDialogProvider
import com.creditclub.analytics.NetworkMetricsInterceptor
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubClient
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.api.RequestFailureInterceptor
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.receipt.PdfPrinter
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 23/09/2019.
 * Appzone Ltd
 */

private const val CACHE_SIZE = 50L * 1024L * 1024L // 50 MB

val dataModule = module {
    single { LocalStorage(androidContext()) }
    single { AppDataStorage.getInstance(androidContext()) }
    single { CoreDatabase.getInstance(androidContext()) }
}

val locationModule = module {
    single { TrackGPS(androidContext(), get()) }
}

val apiModule = module {
    single(named("middleware")) {
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
            .addInterceptor(NetworkMetricsInterceptor(get(), get(), get(), get()))
            .addInterceptor(RequestFailureInterceptor())

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return@single builder.build()
    }

    single { CreditClubMiddleWareAPI(get(named("middleware")), get<AppConfig>().apiHost) }

    single { CreditClubClient(get(named("middleware")), get<AppConfig>().apiHost) }
}

val uiModule = module {
    factory<DialogProvider> { (context: Context) ->
        CreditClubDialogProvider(context)
    }
}

val configModule = module {
    single<IInstitutionConfig> { LocalInstitutionConfig.create(androidContext()) }
    single<AppConfig> {
        object : AppConfig {
            override val apiHost = BuildConfig.API_HOST
            override val posNotificationToken = BuildConfig.NOTIFICATION_TOKEN
            override val appName = androidContext().getString(R.string.app_name)
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