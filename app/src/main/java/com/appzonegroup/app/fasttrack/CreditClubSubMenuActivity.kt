package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.app.AppFunctions
import com.appzonegroup.app.fasttrack.databinding.ActivityCreditClubSubMenuBinding
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.CustomerAccount
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.ActivityMisc
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.creditclub.pos.CardMainMenuActivity
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.util.customerBalanceEnquiry
import com.creditclub.core.util.requireAccountInfo

class CreditClubSubMenuActivity : BaseActivity(), View.OnClickListener {

    private val category: Int
        get() = intent.getIntExtra(CATEGORY_TYPE, 0)

    private lateinit var ah: APIHelper
    private lateinit var bankOneApplication: BankOneApplication
    private lateinit var binding: ActivityCreditClubSubMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_credit_club_sub_menu)
        bankOneApplication = application as BankOneApplication

        ah = APIHelper(baseContext)

        render(category)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run {
            render(getIntExtra(CATEGORY_TYPE, 0))
        }
    }

    private fun render(category: Int) {
        binding.title = AppFunctions.Categories[category]

        when (category) {
            AppFunctions.Categories.CUSTOMER_CATEGORY -> {
                binding.customerCategory.visibility = View.VISIBLE

                binding.customerBalanceEnquiryButton.button.setOnClickListener(this)
                binding.customerChangePinButton.button.setOnClickListener(this)
                binding.customerMiniStatementButton.button.setOnClickListener(this)
                binding.customerBalanceEnquiryButton.button.setOnClickListener(this)
                binding.bvnUpdateButton.button.setOnClickListener(this)
                binding.registerButton.button.setOnClickListener(this)
                binding.newWalletButton.button.setOnClickListener(this)
            }

            AppFunctions.Categories.LOAN_CATEGORY -> {
                binding.loanCategory.visibility = View.VISIBLE

                binding.loanRequestButton.button.setOnClickListener(this)
            }

            AppFunctions.Categories.TRANSACTIONS_CATEGORY -> {
                binding.transactionsCategory.visibility = View.VISIBLE

                binding.depositButton.button.setOnClickListener(this)
                binding.tokenWithdrawalButton.button.setOnClickListener(this)
                binding.payBillButton.button.setOnClickListener(this)
                binding.airtimeButton.button.setOnClickListener(this)
                binding.fundsTransferButton.button.setOnClickListener(this)

                if (Platform.supportsPos()) {
                    binding.cardWithdrawalButton.button.setOnClickListener(this)
                } else {
                    binding.cardWithdrawalButton.button.visibility = View.GONE
                }
            }

            AppFunctions.Categories.AGENT_CATEGORY -> {
                binding.agentCategory.visibility = View.VISIBLE

                binding.agentBalanceEnquiryButton.button.setOnClickListener(this)
                binding.agentChangePinButton.button.setOnClickListener(this)
                binding.agentMiniStatementButton.button.setOnClickListener(this)
                binding.caseLoggingButton.button.setOnClickListener(this)
            }
        }
    }

    override fun onClick(v: View?) {
        v?.apply { openPageById(id) }
    }

    private fun openPageById(id: Int) {
        when (id) {
            R.id.register_button -> startActivity(CustomerRequestOpenAccountActivity::class.java)

            R.id.new_wallet_button -> startActivity(NewWalletActivity::class.java)

            R.id.deposit_button -> startActivity(DepositActivity::class.java)

            R.id.token_withdrawal_button -> startActivity(WithdrawActivity::class.java)

            R.id.card_withdrawal_button -> {
                if (Platform.supportsPos()) {
                    val intent = Intent(this, CardMainMenuActivity::class.java)
                    intent.putExtra("SHOW_BACK_BUTTON", true)
                    startActivity(intent)
                }
            }

            R.id.customer_change_pin_button -> startActivity(ChangeCustomerPinActivity::class.java)

            R.id.agent_balance_enquiry_button -> startActivity(BalanceEnquiryActivity::class.java)

            R.id.agent_change_pin_button -> startActivity(ChangePinActivity::class.java)

            R.id.case_logging_button -> startActivity(CaseLogActivity::class.java)

            R.id.airtime_button -> {
//                startActivity(AirtimeActivity::class.java)

                val billCategory = BillCategory().apply {
                    this.id = "3"
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
                        this@CreditClubSubMenuActivity,
                        BillsCategoryActivity::class.java
                    ).apply {
                        putExtra("customer", CustomerAccount())
                    })
//                    }
//                }
            }

            R.id.customer_balance_enquiry_button -> {
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

            else -> showNotification(
                "This function is not available at the moment. Please look out for it in our next update.",
                false
            )
        }
    }

    fun onRegisterClicked(v: View) {
        ActivityMisc.startActivity(
            this@CreditClubSubMenuActivity,
            CustomerRequestOpenAccountActivity::class.java
        )
    }

    fun onDepositClicked(v: View) {
        ActivityMisc.startActivity(this@CreditClubSubMenuActivity, DepositActivity::class.java)
    }

    fun onWithdrawalClicked(v: View) {
        startActivity(WithdrawActivity::class.java)
    }

    fun onAgentBalanceClicked(v: View) {
        val optionsDialog =
            Dialogs.getDialog(R.layout.dialog_balance_options, this@CreditClubSubMenuActivity)
        optionsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        optionsDialog.setCanceledOnTouchOutside(true)
        optionsDialog.findViewById<View>(R.id.agent_balance).setOnClickListener {
            startActivity(BalanceEnquiryActivity::class.java)
            optionsDialog.dismiss()
        }

        optionsDialog.findViewById<View>(R.id.customer_balance).setOnClickListener {
            startActivity(AccountDetailsActivity::class.java)
            optionsDialog.dismiss()
        }

        optionsDialog.show()
    }

    fun onChangePinClicked(v: View) {
        val optionsDialog =
            Dialogs.getDialog(R.layout.dialog_balance_options, this@CreditClubSubMenuActivity)
        optionsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        optionsDialog.setCanceledOnTouchOutside(true)
        optionsDialog.findViewById<View>(R.id.agent_balance)
            .setOnClickListener { startActivity(ChangePinActivity::class.java) }
        optionsDialog.findViewById<View>(R.id.customer_balance)
            .setOnClickListener { startActivity(ChangeCustomerPinActivity::class.java) }

        optionsDialog.show()
    }

    fun onLoanRequestClicked(v: View) {
        startActivity(CreditClubLoanRequestActivity::class.java)
    }

    fun onBVNUpdateClicked(v: View) {
        ActivityMisc.startActivity(this@CreditClubSubMenuActivity, BVNUpdateActivity::class.java)
    }

    fun onPayBillClicked(view: View) {
        showNotification(
            "This function is not available at the moment. Please look out for it in our next update.",
            false
        )
    }

    fun onBackPressed(v: View?) {
        onBackPressed()
    }

    companion object {
        const val CATEGORY_TYPE = "CATEGORY_TYPE"
    }
}
