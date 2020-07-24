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
    @SerialName("Route") var route: String,
    @SerialName("MinimumAmount") var minimumAmount: Double,
    @SerialName("MaximumAmount") var maximumAmount: Double
)

fun List<BinRoutes>.getSupportedRoute(bin: String, amount: Double): String? {
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

