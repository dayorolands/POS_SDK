package com.creditclub.pos

interface PosConfig {
    var apn: String
    var host: String
    var ip: String
    var port: Int
    var callHome: String
    var terminalId: String
    var supervisorPin: String
    var adminPin: String
    var remoteConnectionInfo: RemoteConnectionInfo
}

interface RemoteConnectionInfo {
    val id: String
    val label: String
    val key1: String
    val key2: String
    val ip: String
    val port: Int
    val ssl: Boolean
    val dukptConfig: DukptConfig?

    /**
     * The request timeout (in seconds)
     */
    val timeout: Int

    val nodeName: String?
    val requeryConfig: RequeryConfig?
}

interface RequeryConfig {
    val timeout: Int
    val maxRetries: Int
}

interface DukptConfig {
    val ipek: String
    val ksn: String
}

val InvalidRemoteConnectionInfo = object : RemoteConnectionInfo {
    override val id: String = "invalid"
    override val label: String = "invalid"
    override val key1: String = ""
    override val key2: String = ""
    override val ip: String = "0.0.0.0"
    override val port: Int = 0
    override val ssl: Boolean = false
    override val dukptConfig: DukptConfig? = null
    override val timeout: Int = 60
    override val nodeName: String? = null
    override val requeryConfig: RequeryConfig? = null
}
