package com.appzonegroup.app.fasttrack.di

import android.content.Context
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.app.LocalInstitutionConfig
import com.appzonegroup.app.fasttrack.ui.CreditClubDialogProvider
import com.creditclub.analytics.NetworkMetricsInterceptor
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubClient
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.debugOnly
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 23/09/2019.
 * Appzone Ltd
 */

val dataModule = module {
    single { LocalStorage.getInstance(androidContext()) }
    single { AppDataStorage.getInstance(androidContext()) }
    single { CoreDatabase.getInstance(androidContext()) }
}

val locationModule = module {
    single { TrackGPS(androidContext()) }
}

val apiModule = module {
    single(named("middleware")) {
        val cacheSize = 10L * 1024 * 1024 // 10 MB
        val cache = Cache(androidContext().cacheDir, cacheSize)
        val certificatePinner = CertificatePinner.Builder()
            .add("api.mybankone.com", "sha256/goId03pe7sxzYmTdNcd1vI+psOY/FX5YGYjkPeioB0w=")
            .build()

        val builder = OkHttpClient().newBuilder()
            .certificatePinner(certificatePinner)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .cache(cache)
            .addInterceptor(NetworkMetricsInterceptor())

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        return@single builder.build()
    }

    single { CreditClubMiddleWareAPI(get(named("middleware")), get<BackendConfig>().apiHost) }

    single { CreditClubClient(get(named("middleware")), get<BackendConfig>().apiHost) }
}

val uiModule = module {
    factory<DialogProvider>(override = true) { (context: Context) ->
        CreditClubDialogProvider(context)
    }
}

val configModule = module {
    single<IInstitutionConfig> { LocalInstitutionConfig.create(androidContext()) }
    single<BackendConfig> {
        object : BackendConfig {
            override val apiHost = BuildConfig.API_HOST
            override val posNotificationToken = BuildConfig.NOTIFICATION_TOKEN
        }
    }
}