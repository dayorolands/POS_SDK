package com.creditclub.core.data.api


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 06/02/2020.
 * Appzone Ltd
 */
interface AppConfig {
    val apiHost: String
    val posNotificationToken: String
    val appName: String
    val otaUpdateId: String
    val versionName: String
    val versionCode: Int
    val fileProviderAuthority: String
}