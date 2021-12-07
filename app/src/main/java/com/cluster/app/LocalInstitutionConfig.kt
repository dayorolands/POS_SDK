package com.cluster.app

import android.content.Context
import com.cluster.BuildConfig
import com.cluster.R
import com.creditclub.core.config.*
import com.creditclub.core.type.TransactionType
import com.creditclub.core.util.localStorage

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 17/10/2019.
 * Appzone Ltd
 */
class LocalInstitutionConfig private constructor(
    override var name: String,
    override var hasOnlineFunctions: Boolean,
    override var hasHlaTagging: Boolean,
    override var transactionTypes: List<TransactionType>,
    override var flows: FlowConfig,
    override var categories: CategoryConfig,
    override var bankAccountNumberLength: Int,
) : InstitutionConfig {

    companion object {

        fun create(context: Context): LocalInstitutionConfig {
            val resources = context.resources
            val config = LocalInstitutionConfig(
                hasOnlineFunctions = resources.getBoolean(R.bool.online_functions_enabled),
                hasHlaTagging = resources.getBoolean(R.bool.hla_enabled),
                name = resources.getString(R.string.institution_name),
                categories = CategoryConfig(
                    loans = resources.getBoolean(R.bool.category_loan),
                    customers = resources.getBoolean(R.bool.category_customer),
                ),
                transactionTypes = resources.getStringArray(R.array.transaction_types).map {
                    TransactionType.valueOf(it)
                },
                bankAccountNumberLength = 10,
                flows = FlowConfig(),
            )

            config.flows.run {
                if (resources.getBoolean(R.bool.flow_token_withdrawal)) {
                    tokenWithdrawal = TokenWithdrawalConfig(
                        customerPin = resources.getBoolean(R.bool.token_withdrawal_customer_pin),
                        externalToken = resources.getBoolean(R.bool.token_withdrawal_external_token),
                    )
                }

                if (resources.getBoolean(R.bool.flow_token_withdrawal)) {
                    accountOpening = AccountOpeningConfig(
                        products = resources.getBoolean(R.bool.account_opening_products),
                    )
                }

                if (resources.getBoolean(R.bool.flow_bvn_update)) {
                    bvnUpdate = Any()
                }

                if (resources.getBoolean(R.bool.flow_customer_pin_change)) {
                    customerPinChange = Any()
                }

                if (resources.getBoolean(R.bool.flow_wallet_opening)) {
                    walletOpening = AccountOpeningConfig()
                }

                if (resources.getBoolean(R.bool.flow_customer_balance)) {
                    customerBalance = Any()
                }

                if (resources.getBoolean(R.bool.flow_airtime)) {
                    airtime = Any()
                }

                if (resources.getBoolean(R.bool.collections_enabled)) {
                    collectionPayment = Any()
                }

                if (resources.getBoolean(R.bool.bill_payment_enabled)) {
                    billPayment = Any()
                }

                if (resources.getBoolean(R.bool.flow_ussd_withdrawal)) {
                    ussdWithdrawal = Any()
                }
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
                    config.categories = config.categories.copy(loans = false)
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