package com.creditclub.pos.model

import com.creditclub.pos.DukptConfig
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.RequeryConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinRoutes(
    @SerialName("BIN") var bin: String,
    @SerialName("Routes") var routes: List<Route>
)

@Serializable
data class Route(
    @SerialName("Route") var connectionInfo: ConnectionInfo,
    @SerialName("MinimumAmount") var minimumAmount: Double,
    @SerialName("MaximumAmount") var maximumAmount: Double
)

@Serializable
data class ConnectionInfo(
    @SerialName("Name") override val nodeName: String,
    @SerialName("IPAddress") override val ip: String,
    @SerialName("Port") override val port: Int,
    @SerialName("EnableSSL") override val ssl: Boolean,
    @SerialName("Dukpt") override val dukptConfig: DukptConfigImpl? = null,
    @SerialName("Timeout") override val timeout: Int = 90,
    @SerialName("RequeryConfig") override val requeryConfig: RequeryConfigImpl? = RequeryConfigImpl(
        timeout = 90,
        maxRetries = 1
    ),
) : RemoteConnectionInfo {
    override val id: String get() = nodeName
    override val label: String get() = nodeName
    override val key1: String get() = "3DFB3802940E8A546B3D38610852BA7A"
    override val key2: String get() = "0234E39861D3405E7A6B3185BA675873"
}

@Serializable
data class DukptConfigImpl(
    @SerialName("IPEK") override val ipek: String,
    @SerialName("KSN") override val ksn: String
) : DukptConfig

@Serializable
data class RequeryConfigImpl(
    @SerialName("Timeout") override val timeout: Int,
    @SerialName("MaxRetries") override val maxRetries: Int
) : RequeryConfig

fun List<BinRoutes>.getSupportedRoute(bin: String, amount: Double): ConnectionInfo? {
    for (binRoutes in this) {
        if (bin.startsWith(binRoutes.bin)) {
            for (route in binRoutes.routes) {
                if (amount >= route.minimumAmount && amount <= route.maximumAmount) {
                    return route.connectionInfo
                }
            }
        }
    }

    return null
}