package com.appzonegroup.app.fasttrack.model

import com.appzonegroup.app.fasttrack.BankOneApplication
import com.appzonegroup.app.fasttrack.R
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/14/2019.
 * Appzone Ltd
 */

class CustomerAccount : Serializable {
    @SerializedName("Name")
    var name: String = ""

    @SerializedName("PhoneNumber")
    var phoneNumber = ""

    @SerializedName("LinkingBankAccounts")
    var linkingBankAccounts: ArrayList<AccountInfo> = ArrayList()
}
