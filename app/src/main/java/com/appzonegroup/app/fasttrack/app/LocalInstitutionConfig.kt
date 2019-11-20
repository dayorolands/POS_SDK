package com.appzonegroup.app.fasttrack.app

import android.content.Context
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.config.CategoryConfig
import com.creditclub.core.config.FlowConfig
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.type.TransactionType

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 17/10/2019.
 * Appzone Ltd
 */
class LocalInstitutionConfig : IInstitutionConfig {

    override var hasOnlineFunctions: Boolean = false

    override var transactionTypes: List<TransactionType> = TransactionType.values().toList()

    override var flows: FlowConfig = FlowConfig()

    override var categories: CategoryConfig = CategoryConfig()

    companion object {

        fun create(context: Context): LocalInstitutionConfig {
            val config = LocalInstitutionConfig()
            val resources = context.resources

            config.hasOnlineFunctions = resources.getBoolean(R.bool.online_functions_enabled)

            config.flows.run {
                tokenWithdrawal.customerPin =
                    resources.getBoolean(R.bool.token_withdrawal_customer_pin)

                if (!resources.getBoolean(R.bool.flow_bvn_update)) {
                    bvnUpdate = null
                }

                if (!resources.getBoolean(R.bool.flow_customer_pin_change)) {
                    customerPinChange = null
                }

                if (!resources.getBoolean(R.bool.flow_wallet_opening)) {
                    walletOpening = null
                }
            }

            config.categories.run {
                loans = resources.getBoolean(R.bool.category_loan)
            }

            return config
        }
    }
}