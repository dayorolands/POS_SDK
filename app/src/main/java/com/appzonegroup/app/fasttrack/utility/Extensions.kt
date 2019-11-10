package com.appzonegroup.app.fasttrack.utility

import android.app.Activity
import android.content.Intent
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.CustomerAccount
import com.appzonegroup.creditclub.pos.CardMainMenuActivity
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.util.customerBalanceEnquiry
import com.creditclub.core.util.logFunctionUsage
import com.creditclub.core.util.requireAccountInfo
import kotlinx.coroutines.launch


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 19/10/2019.
 * Appzone Ltd
 */

fun Activity.logout(block: (Intent.() -> Unit)? = null) {
    val intent = Intent(applicationContext, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (block != null) intent.apply(block)

    finish()
    startActivity(intent)
}

fun BaseActivity.openPageById(id: Int) {
    when (id) {
        R.id.register_button -> startActivity(CustomerRequestOpenAccountActivity::class.java)

        R.id.new_wallet_button -> startActivity(NewWalletActivity::class.java)

        R.id.deposit_button -> startActivity(DepositActivity::class.java)

        R.id.token_withdrawal_button -> startActivity(WithdrawActivity::class.java)

        R.id.card_withdrawal_button -> {
            if (Platform.isPOS) {
                ioScope.launch { logFunctionUsage(FunctionIds.CARD_TRANSACTIONS) }

                val intent = Intent(this, CardMainMenuActivity::class.java)
                intent.putExtra("SHOW_BACK_BUTTON", true)
                startActivity(intent)
            }
        }

        R.id.customer_change_pin_button -> startActivity(ChangeCustomerPinActivity::class.java)

        R.id.agent_balance_enquiry_button -> {
            ioScope.launch { logFunctionUsage(FunctionIds.AGENT_BALANCE_ENQUIRY) }

            startActivity(BalanceEnquiryActivity::class.java)
        }

        R.id.agent_change_pin_button -> startActivity(ChangePinActivity::class.java)

        R.id.case_logging_button -> startActivity(CaseLogActivity::class.java)

        R.id.airtime_button -> {

            ioScope.launch { logFunctionUsage(FunctionIds.AIRTIME_RECHARGE) }

            val billCategory = BillCategory().apply {
                this.id = getString(R.string.bills_airtime_category_id)
                name = "Mobile Recharge"
                description = "Recharge your phone"
            }

            val i = Intent(this, BillerActivity::class.java).apply {
                putExtra("categoryId", billCategory.id)
                LocalStorage.SaveValue(AppConstants.CATEGORYID, billCategory.id, baseContext)

                putExtra("categoryName", billCategory.name)
                LocalStorage.SaveValue(
                    AppConstants.CATEGORYNAME,
                    billCategory.name,
                    baseContext
                )

                putExtra("propertyChanged", billCategory.propertyChanged)
                LocalStorage.SaveValue(
                    AppConstants.PROPERTYCHANGED,
                    billCategory.propertyChanged,
                    baseContext
                )

                putExtra("customer", intent.getSerializableExtra("customer"))
                putExtra("isAirtime", true)
            }

            startActivityForResult(i, 1)
        }

        R.id.pay_bill_button -> {
//                getCustomer {
//                    onSubmit { (customer) ->
            startActivity(
                Intent(
                    this, BillsCategoryActivity::class.java
                ).apply {
                    putExtra("customer", CustomerAccount())
                })
//                    }
//                }
        }

        R.id.customer_balance_enquiry_button -> {
            ioScope.launch { logFunctionUsage(FunctionIds.CUSTOMER_BALANCE_ENQUIRY) }
//                startActivity(AccountDetailsActivity::class.java)
            requireAccountInfo(available = arrayOf(CustomerRequestOption.AccountNumber)) {
                onSubmit { accountInfo ->
                    customerBalanceEnquiry(accountInfo)
                }
            }
        }

        R.id.loan_request_button -> startActivity(CreditClubLoanRequestActivity::class.java)

        R.id.bvn_update_button -> startActivity(BVNUpdateActivity::class.java)

        R.id.funds_transfer_button -> startActivity(FundsTransferActivity::class.java)

        R.id.agent_mini_statement_button -> startActivity(MiniStatementActivity::class.java)

        R.id.fn_support, R.id.support -> startActivity(SupportActivity::class.java)

        R.id.fn_hla_tagging -> startActivity(HlaTaggingActivity::class.java)

        else -> showNotification(
            "This function is not available at the moment. Please look out for it in our next update.",
            false
        )
    }
}