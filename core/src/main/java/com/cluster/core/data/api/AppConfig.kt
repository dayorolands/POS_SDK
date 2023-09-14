package com.cluster.core.data.api

interface AppConfig {
    val apiHost: String
    val posNotificationToken: String
    val appName: String
    val otaUpdateId: String
    val versionName: String
    val versionCode: Int
    val fileProviderAuthority: String
}