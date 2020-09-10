package com.creditclub.analytics.models

import com.creditclub.core.serializer.TimeInstantConverter
import com.creditclub.core.serializer.TimeInstantSerializer
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.Instant

@Entity
@Serializable
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

    @SerialName("Url")
    var url: String? = null

    @SerialName("Method")
    var method: String? = null

    @SerialName("GPSCoordinates")
    var gpsCoordinates: String? = null

    @SerialName("StatusCode")
    var statusCode: Int = 0

    @SerialName("AppName")
    var appName: String? = null

    @SerialName("AppVersionCode")
    var appVersion: String? = null

    @SerialName("Message")
    var message: String? = null

    @SerialName("Duration")
    var duration: Long = 0

    @Serializable(with = TimeInstantSerializer::class)
    @Convert(converter = TimeInstantConverter::class, dbType = String::class)
    @SerialName("RequestTime")
    var requestTime: Instant? = null

    @Serializable(with = TimeInstantSerializer::class)
    @Convert(converter = TimeInstantConverter::class, dbType = String::class)
    @SerialName("ResponseTime")
    var responseTime: Instant? = null
}