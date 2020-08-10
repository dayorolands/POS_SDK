package com.appzonegroup.creditclub.pos.extension

import com.creditclub.core.data.model.ConnectionInfo
import com.creditclub.pos.RemoteConnectionInfo

fun ConnectionInfo.toRemoteConnectionInfo() = object : RemoteConnectionInfo {
    override val id: String
        get() = this@toRemoteConnectionInfo.id
    override val label: String
        get() = this@toRemoteConnectionInfo.label
    override val key1: String
        get() = this@toRemoteConnectionInfo.key1
    override val key2: String
        get() = this@toRemoteConnectionInfo.key2
    override val ip: String
        get() = this@toRemoteConnectionInfo.ip
    override val port: Int
        get() = this@toRemoteConnectionInfo.port
    override val ssl: Boolean
        get() = this@toRemoteConnectionInfo.ssl
}