package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinRoutes(
    @SerialName("BIN") var bin: String,
    @SerialName("Routes") var routes: List<Route>
)

@Serializable
data class Route(
    @SerialName("Route") var route: ConnectionInfo,
    @SerialName("MinimumAmount") var minimumAmount: Double,
    @SerialName("MaximumAmount") var maximumAmount: Double
)

@Serializable
data class ConnectionInfo(
    @SerialName("Name") val id: String,
    @SerialName("IPAddress") val ip: String,
    @SerialName("Port") val port: Int,
    @SerialName("EnableSSL") val ssl: Boolean
) {
    val label: String get() = id
    val key1: String get() = "3DFB3802940E8A546B3D38610852BA7A"
    val key2: String get() = "0234E39861D3405E7A6B3185BA675873"
}

fun List<BinRoutes>.getSupportedRoute(bin: String, amount: Double): ConnectionInfo? {
    for (binRoutes in this) {
        if (bin.startsWith(binRoutes.bin)) {
            for (route in binRoutes.routes) {
                if (amount <= route.minimumAmount && amount >= route.maximumAmount) {
                    return route.route
                }
            }
        }
    }

    return null
}

