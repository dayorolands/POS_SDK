package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.databinding.ActivityCreditClubSubMenuBinding
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions

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

                val flows = institutionConfig.flows

                if (flows.walletOpening == null) {
                    binding.newWalletButton.root.visibility = View.GONE
                }

                if (flows.bvnUpdate == null) {
                    binding.bvnUpdateButton.root.visibility = View.GONE
                }

                if (flows.customerPinChange == null) {
                    binding.customerChangePinButton.root.visibility = View.GONE
                }
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
                binding.collectionPaymentButton.button.setOnClickListener(this)

                if (Platform.isPOS) {
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
        v?.run { openPageById(id) }
    }

    fun onBackPressed(v: View?) {
        onBackPressed()
    }

    companion object {
        const val CATEGORY_TYPE = "CATEGORY_TYPE"
    }
}
