package com.appzonegroup.app.fasttrack.app

import android.content.Context
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.config.CategoryConfig
import com.creditclub.core.config.FlowConfig
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.type.TransactionType
import com.creditclub.core.util.localStorage

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 17/10/2019.
 * Appzone Ltd
 */
class LocalInstitutionConfig private constructor() : IInstitutionConfig {

    override lateinit var name: String

    override var hasOnlineFunctions: Boolean = false

    override var hasHlaTagging: Boolean = false

    override var transactionTypes: List<TransactionType> = emptyList()

    override var flows: FlowConfig = FlowConfig()

    override var categories: CategoryConfig = CategoryConfig()

    override var bankAccountNumberLength: Int = 10

    companion object {

        fun create(context: Context): LocalInstitutionConfig {
            val config = LocalInstitutionConfig()
            val resources = context.resources

            config.hasOnlineFunctions = resources.getBoolean(R.bool.online_functions_enabled)
            config.hasHlaTagging = resources.getBoolean(R.bool.hla_enabled)
            config.name = resources.getString(R.string.institution_name)

            config.flows.run {
                tokenWithdrawal.customerPin =
                    resources.getBoolean(R.bool.token_withdrawal_customer_pin)

                tokenWithdrawal.externalToken =
                    resources.getBoolean(R.bool.token_withdrawal_external_token)

                accountOpening.products =
                    resources.getBoolean(R.bool.account_opening_products)

                if (!resources.getBoolean(R.bool.flow_bvn_update)) {
                    bvnUpdate = null
                }

                if (!resources.getBoolean(R.bool.flow_customer_pin_change)) {
                    customerPinChange = null
                }

                if (!resources.getBoolean(R.bool.flow_wallet_opening)) {
                    walletOpening = null
                }

                if (!resources.getBoolean(R.bool.collections_enabled)) {
                    collectionPayment = null
                }

                if (!resources.getBoolean(R.bool.bill_payment_enabled)) {
                    billPayment = null
                }
            }

            config.categories.run {
                loans = resources.getBoolean(R.bool.category_loan)
                customers = resources.getBoolean(R.bool.category_customer)
            }

            config.transactionTypes = resources.getStringArray(R.array.transaction_types).map {
                TransactionType.valueOf(it)
            }

            // Manual overrides for creditclub variant
            if (BuildConfig.FLAVOR == "creditclub") {
                val institutionCode = context.localStorage.institutionCode
                val isTcfBank = institutionCode == "100568" || institutionCode == "100309"
                val isSterlingBank = institutionCode == "100567"

                config.name = when (institutionCode) {
                    "100567" -> "Sterling Bank"
                    "100568", "100309" -> "TCF MFB"
                    else -> "My Bank"
                }

                if (isTcfBank) {
                    config.categories.loans = false
                    config.hasHlaTagging = false
                    config.hasOnlineFunctions = false
                    config.flows.bvnUpdate = null
                }

                if (isSterlingBank) {
                    config.flows.billPayment = null
                }
            }

            return config
        }
    }
}