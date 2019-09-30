package com.appzonegroup.app.fasttrack.utility

import com.appzonegroup.app.fasttrack.model.AccountInfo
import com.appzonegroup.app.fasttrack.model.CustomerAccount
import com.creditclub.core.ui.widget.DialogProvider
import com.google.gson.Gson


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/14/2019.
 * Appzone Ltd
 */

object CustomerHelper {
    fun processCustomerAccount(output: String?, dialogProvider: DialogProvider): CustomerAccount? {
        if (output != null) {
            val customerAccount = Gson().fromJson(output, CustomerAccount::class.java)
                    ?: return dialogProvider.run {
                        showError("There was a problem getting the customer's account(s) information")
                        null
                    }

            if (customerAccount.linkingBankAccounts.size > 0) {
                return customerAccount
            } else {
                dialogProvider.showError("The customer does not have any account")
            }
        } else {
            dialogProvider.showError("An error occurred while fetching the customer's account(s)")
        }

        return null
    }


    fun processCustomerAccountInfo(output: String?, dialogProvider: DialogProvider): CustomerAccount? {
        if (output != null) {
            val accountInfo = Gson().fromJson(output, AccountInfo::class.java)
                    ?: return dialogProvider.run {
                        showError("The customer does not have any account")
                        null
                    }

            return CustomerAccount().apply {
                name = accountInfo.accountName
                phoneNumber = accountInfo.phoneNumber
                linkingBankAccounts = arrayListOf(accountInfo)
            }
        } else {
            dialogProvider.showError("An error occurred while fetching the customer's account(s)")
        }

        return null
    }
}