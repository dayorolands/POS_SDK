package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */

@Serializable
class Feedback {
    @SerialName("ID")
    var id: Int? = null

    @SerialName("DisplayMessage")
    var displayMessage: Int? = null

    @SerialName("DateLogged")
    var dateLogged: String? = null

    @SerialName("CaseReference")
    var caseReference: String? = null

    @SerialName("Message")
    var message: String? = null

    @SerialName("IsAgent")
    var isAgent = true

    @SerialName("IsActive")
    var isActive = true

    @SerialName("IsASystemChange")
    var isASystemChange = true

    @SerialName("Name")
    var name: String? = null

    @SerialName("LastReadTime")
    var lastReadTime: String? = null
}