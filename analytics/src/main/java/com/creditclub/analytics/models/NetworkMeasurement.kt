package com.creditclub.analytics.models

import com.creditclub.core.serializer.TimeInstantSerializer
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.Instant

@Entity
class NetworkMeasurement {
    @Id
    @SerialName("ID")
    var id: Long = 0

    @SerialName("AgentCode")
    var agentCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("Path")
    var path: String? = null

    @SerialName("Host")
    var host: String? = null

    @SerialName("Scheme")
    var scheme: String? = null

    @SerialName("Method")
    var method: String? = null

    @SerialName("GPSCoordinates")
    var gpsCoordinates: String? = null

    @SerialName("StatusCode")
    var statusCode: Int = 0

    @SerialName("Duration")
    var duration = 0.0

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("RequestTime")
    var requestTime: Instant = Instant.now()

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("ResponseTime")
    var responseTime: Instant? = null
}