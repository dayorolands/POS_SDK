package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoanRequestCreditClub(
    @SerialName("LoanAmount")
    val loanAmount: Double = 0.0,

    @SerialName("LoanProductID")
    val loanProductID: Int = 0,

    @SerialName("AssociationID")
    val associationID: String? = null,

    @SerialName("MemberID")
    val memberID: String? = null,

    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String? = null,

    @SerialName("CustomerPhoneNumber")
    val customerPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("LoanProductInstitutionCode")
    val loanProductInstitutionCode: String? = null,

    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,
)