package com.creditclub.core.data.api

import com.creditclub.core.data.model.AppVersion
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
interface VersionService {
    @GET("api/Version/GetLatestVersionAndDownloadLink")
    suspend fun getLatestVersionAndDownloadLink(@Query("appName") appName: String?): AppVersion?
}