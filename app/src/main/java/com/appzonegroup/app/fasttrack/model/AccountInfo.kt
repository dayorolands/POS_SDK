package com.appzonegroup.app.fasttrack.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/8/2019.
 * Appzone Ltd
 */

class AccountInfo : Serializable {
    @SerializedName("AccountName")
    var accountName: String = ""

    @SerializedName("Number")
    var number: String = ""

    @SerializedName("PhoneNumber")
    var phoneNumber: String = ""
}
