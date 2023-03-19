package com.cluster.pos

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
    val host: String
    val port: Int
    val sslEnabled: Boolean
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
        get() = "3F2216D8297BCE9C"
    val ksn: String
        get() = "0000000002DDDDE00001"
}

object InvalidRemoteConnectionInfo : RemoteConnectionInfo {
    override val id: String = "invalid"
    override val label: String = "invalid"
    override val key1: String = ""
    override val key2: String = ""
    override val host: String = "0.0.0.0"
    override val port: Int = 0
    override val sslEnabled: Boolean = false
    override val dukptConfig: DukptConfig? = null
    override val timeout: Int = 60
    override val nodeName: String? = null
    override val requeryConfig: RequeryConfig? = null
}
