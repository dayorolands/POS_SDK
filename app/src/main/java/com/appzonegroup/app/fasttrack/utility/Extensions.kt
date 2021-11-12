package com.appzonegroup.app.fasttrack.utility

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.debug
import com.creditclub.core.util.logFunctionUsage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.ui.rememberBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun Activity.logout() {
    val intent = Intent(applicationContext, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    intent.putExtra("LOGGED_OUT", true)

    finish()
    startActivity(intent)
}

@Composable
fun FunctionUsageTracker(fid: Int) {
    val coreDatabase: CoreDatabase by rememberBean()
    var usageHasBeenLogged by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fid) {
        if (!usageHasBeenLogged) {
            withContext(Dispatchers.IO) {
                val appFunctionUsageDao = coreDatabase.appFunctionUsageDao()
                val appFunction = appFunctionUsageDao.getFunction(fid)

                val count = if (appFunction == null) {
                    appFunctionUsageDao.insert(AppFunctionUsage(fid))
                    1
                } else {
                    appFunction.usage++
                    appFunctionUsageDao.update(appFunction)

                    appFunction.usage
                }
                usageHasBeenLogged = true
                debug("Usage for function $fid -> $count")
            }
        }
    }
}

fun CreditClubFragment.openPageById(id: Int) {
    when (id) {
        R.id.register_button -> startActivity(CustomerRequestOpenAccountActivity::class.java)

        R.id.new_wallet_button -> startActivity(NewWalletActivity::class.java)

        R.id.deposit_button -> startActivity(DepositActivity::class.java)

        R.id.token_withdrawal_button -> startActivity(WithdrawActivity::class.java)

        R.id.card_withdrawal_button -> {
            if (Platform.isPOS) {
                ioScope.launch { logFunctionUsage(FunctionIds.CARD_TRANSACTIONS) }

                findNavController().navigate(R.id.action_to_pos_nav_graph)
            }
        }

        R.id.customer_change_pin_button -> startActivity(ChangeCustomerPinActivity::class.java)

        R.id.agent_balance_enquiry_button -> {
            val staticService: StaticService by retrofitService()
            mainScope.launch {
                val pin = dialogProvider.getPin("Agent PIN") ?: return@launch
                if (pin.length != 4) {
                    dialogProvider.showError("Agent PIN must be 4 digits")
                    return@launch
                }
                val request = BalanceEnquiryRequest(
                    agentPin = pin,
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    deviceNumber = localStorage.deviceNumber,
                )

                dialogProvider.showProgressBar("Getting Balance")
                val (response) = safeRunIO {
                    staticService.balanceEnquiry(request)
                }
                dialogProvider.hideProgressBar()

                if (response == null) return@launch

                if (response.isSussessful) {
                    dialogProvider.showSuccess(
                        """
                        |Balance is ${response.availableBalance.toCurrencyFormat()}.
                        |Available balance is ${response.balance.toCurrencyFormat()}.
                    """.trimMargin()
                    )
                } else {
                    dialogProvider.showError(
                        response.responseMessage ?: getString(R.string.a_network_error_occurred)
                    )
                }
                safeRunIO { logFunctionUsage(FunctionIds.AGENT_BALANCE_ENQUIRY) }
            }
        }

        R.id.agent_change_pin_button -> startActivity(ChangePinActivity::class.java)

        R.id.case_logging_button -> startActivity(CaseLogActivity::class.java)

        R.id.airtime_button -> {

            ioScope.launch { logFunctionUsage(FunctionIds.AIRTIME_RECHARGE) }

            findNavController().navigate(
                R.id.action_to_bill_payment,
                bundleOf("isAirtime" to true)
            )
        }

        R.id.pay_bill_button -> {
            findNavController().navigate(R.id.action_to_bill_payment)
        }

        R.id.fn_bill_payment -> {
            findNavController().navigate(R.id.action_home_to_bill_payment)
        }

        R.id.customer_balance_enquiry_button -> {
            ioScope.launch { logFunctionUsage(FunctionIds.CUSTOMER_BALANCE_ENQUIRY) }
//                startActivity(AccountDetailsActivity::class.java)
            (requireActivity() as CreditClubActivity).requireAccountInfo(
                options = listOf(
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

        R.id.funds_transfer_button -> {
            findNavController().navigate(R.id.action_to_funds_transfer)
        }

        R.id.agent_mini_statement_button -> startActivity(MiniStatementActivity::class.java)

        R.id.fn_hla_tagging -> startActivity(HlaTaggingActivity::class.java)

        R.id.fn_faq -> startActivity(FaqActivity::class.java)

        R.id.collection_payment_button, R.id.fn_collections_payment -> {
            findNavController().navigate(
                R.id.action_to_collection_payment,
                null,
                navOptions = navOptions { restoreState = false },
            )
        }

        R.id.fn_pos_chargeback -> {
            findNavController().navigate(R.id.action_to_chargeback)
        }

        else -> dialogProvider.showError(
            "This function is not available at the moment. Please look out for it in our next update."
        )
    }
}