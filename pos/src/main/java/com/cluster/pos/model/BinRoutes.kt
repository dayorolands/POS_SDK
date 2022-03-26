package com.cluster.pos.model

import android.os.Parcelable
import com.cluster.pos.DukptConfig
import com.cluster.pos.RemoteConnectionInfo
import com.cluster.pos.RequeryConfig
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DEFAULT_COMPONENT_KEY_1 = "3DFB3802940E8A546B3D38610852BA7A"
const val DEFAULT_COMPONENT_KEY_2 = "0234E39861D3405E7A6B3185BA675873"

fun nibssNodeNameSet() = setOf("EPMS", "POSVAS", "EPMS_TEST", "POSVAS_TEST")

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
@Parcelize
data class ConnectionInfo(
    @SerialName("Name") override val nodeName: String,
    @SerialName("IPAddress") override val host: String,
    @SerialName("Port") override val port: Int,
    @SerialName("EnableSSL") override val sslEnabled: Boolean,
    @SerialName("Dukpt") override val dukptConfig: DukptConfigImpl? = null,
    @SerialName("Timeout") override val timeout: Int = 90,
    @SerialName("RequeryConfig") override val requeryConfig: RequeryConfigImpl? = RequeryConfigImpl(
        timeout = 90,
        maxRetries = 1
    ),
    @SerialName("Key1") override val key1: String = DEFAULT_COMPONENT_KEY_1,
    @SerialName("Key2") override val key2: String = DEFAULT_COMPONENT_KEY_2,
) : RemoteConnectionInfo, Parcelable {
    override val id: String get() = nodeName
    override val label: String get() = nodeName
}

@Parcelize
@Serializable
data class PosTenant(@SerialName("Routes") val infoList: List<ConnectionInfo>) : Parcelable

@Serializable
@Parcelize
data class DukptConfigImpl(
    @SerialName("IPEK") override val ipek: String,
    @SerialName("KSN") override val ksn: String
) : DukptConfig, Parcelable

@Serializable
@Parcelize
data class RequeryConfigImpl(
    @SerialName("Timeout") override val timeout: Int,
    @SerialName("MaxRetries") override val maxRetries: Int
) : RequeryConfig, Parcelable

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
