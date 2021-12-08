package com.cluster.core.data.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LoanProduct(
    @SerialName("ID")
    val id: Long = 0,

    @SerialName("Name")
    val name: String? = null,

    @SerialName("MinimumAmount")
    val minimumAmount: Double = 0.0,

    @SerialName("MaximumAmount")
    val maximumAmount: Double = 0.0,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,
) {
    override fun toString() = "$name (N$minimumAmount - N$maximumAmount)"
}