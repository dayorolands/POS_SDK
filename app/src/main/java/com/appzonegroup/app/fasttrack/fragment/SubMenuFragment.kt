package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.SubMenuFragmentBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions
import com.creditclub.core.ui.CreditClubFragment

class SubMenuFragment : CreditClubFragment(R.layout.sub_menu_fragment),
    View.OnClickListener {

    private val binding by dataBinding<SubMenuFragmentBinding>()
    private val args by navArgs<SubMenuFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        render(args.category)
    }

    private fun render(category: Int) {
        binding.title = AppFunctions.Categories[category]


        val flows = institutionConfig.flows
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

                if (flows.collectionPayment == null) {
                    binding.collectionPaymentButton.root.visibility = View.GONE
                }

                if (flows.billPayment == null) {
                    binding.payBillButton.root.visibility = View.GONE
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
}
