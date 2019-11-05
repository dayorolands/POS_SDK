package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CaseDetail {
    @SerialName("ID")
    var id = ""

    @SerialName("Description")
    var description: String? = null

    @SerialName("Subject")
    var subject = "test"

    @SerialName("DateLogged")
    var dateLogged: String? = null

    @SerialName("CaseReporterEmail")
    var caseReporterEmail: String? = null

    @SerialName("CaseReference")
    var caseReference = ""

    @SerialName("CategoryName")
    var categoryName = ""

    @SerialName("InstitutionCode")
    var institutionCode = ""

    @SerialName("Product")
    var product = ""

    @SerialName("IsResolved")
    var isResolved: Boolean? = null

    @SerialName("IsClosed")
    var isClosed: Boolean? = null
}
