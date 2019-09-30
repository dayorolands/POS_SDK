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

    val accountNames: ArrayList<String>
        get() {
            val names = ArrayList<String>()
            names.add(BankOneApplication.getInstance().getString(R.string.please_select_customer_account))
            for (accountInfo in linkingBankAccounts) {
                names.add(String.format(Locale.getDefault(), "%s - %s", accountInfo.accountName, accountInfo.number))
            }

            return names
        }
}
