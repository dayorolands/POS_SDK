package com.appzonegroup.app.fasttrack.utility

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.fragment.HomeFragmentDirections
import com.appzonegroup.app.fasttrack.fragment.SubMenuFragmentDirections
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.creditclub.pos.CardMainMenuActivity
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.CreditClubFragment
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

    intent.putExtra("LOGGED_OUT", true)

    if (block != null) intent.apply(block)

    finish()
    startActivity(intent)
}

private val Fragment.baseContext get() = requireContext()
private suspend fun Fragment.logFunctionUsage(fid: Int) = requireContext().logFunctionUsage(fid)

fun CreditClubFragment.openPageById(id: Int) {
    when (id) {
        R.id.register_button -> startActivity(CustomerRequestOpenAccountActivity::class.java)

        R.id.new_wallet_button -> startActivity(NewWalletActivity::class.java)

        R.id.deposit_button -> startActivity(DepositActivity::class.java)

        R.id.token_withdrawal_button -> startActivity(WithdrawActivity::class.java)

        R.id.card_withdrawal_button -> {
            if (Platform.isPOS) {
                ioScope.launch { logFunctionUsage(FunctionIds.CARD_TRANSACTIONS) }

                val intent = Intent(requireContext(), CardMainMenuActivity::class.java)
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

            val i = Intent(requireContext(), BillerActivity::class.java).apply {
                putExtra("categoryId", billCategory.id)
                BillsLocalStorage.SaveValue(AppConstants.CATEGORYID, billCategory.id, baseContext)

                putExtra("categoryName", billCategory.name)
                BillsLocalStorage.SaveValue(
                    AppConstants.CATEGORYNAME,
                    billCategory.name,
                    baseContext
                )

                putExtra("propertyChanged", billCategory.propertyChanged)
                BillsLocalStorage.SaveValue(
                    AppConstants.PROPERTYCHANGED,
                    billCategory.propertyChanged,
                    baseContext
                )
                putExtra("isAirtime", true)
            }

            startActivityForResult(i, 1)
        }

        R.id.pay_bill_button -> {
            findNavController().navigate(SubMenuFragmentDirections.actionMenuToBillPayment())
        }

        R.id.fn_bill_payment -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeToBillPayment())
        }

        R.id.customer_balance_enquiry_button -> {
            ioScope.launch { logFunctionUsage(FunctionIds.CUSTOMER_BALANCE_ENQUIRY) }
//                startActivity(AccountDetailsActivity::class.java)
            (requireActivity() as CreditClubActivity).requireAccountInfo(
                options = arrayOf(
                    CustomerRequestOption.AccountNumber
                )
            ) {
                onSubmit { accountInfo ->
                    mainScope.launch {
                        (requireActivity() as CreditClubActivity).customerBalanceEnquiry(accountInfo)
                    }
                }
            }
        }

        R.id.loan_request_button -> startActivity(CreditClubLoanRequestActivity::class.java)

        R.id.bvn_update_button -> startActivity(BVNUpdateActivity::class.java)

        R.id.funds_transfer_button -> startActivity(FundsTransferActivity::class.java)

        R.id.agent_mini_statement_button -> startActivity(MiniStatementActivity::class.java)

        R.id.fn_support, R.id.support -> startActivity(SupportActivity::class.java)

        R.id.fn_hla_tagging -> startActivity(HlaTaggingActivity::class.java)

        R.id.fn_faq -> startActivity(FaqActivity::class.java)

        R.id.collection_payment_button -> {
            findNavController().navigate(R.id.action_menu_to_collection_payment)
        }

        R.id.fn_collections_payment -> {
            findNavController().navigate(R.id.action_home_to_collection_payment)
        }

        else -> dialogProvider.showError(
            "This function is not available at the moment. Please look out for it in our next update."
        )
    }
}