package com.cluster.config

import android.content.Context
import com.cluster.BuildConfig
import com.cluster.R
import com.cluster.core.config.*
import com.cluster.core.type.TransactionType
import com.cluster.core.util.delegates.getArrayList
import com.cluster.core.util.localStorage

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 17/10/2019.
 * Appzone Ltd
 */
class LocalInstitutionConfig private constructor(
    override var name: String,
    override var hasOnlineFunctions: Boolean,
    override var hasHlaTagging: Boolean,
    override var transactionTypes: List<TransactionType>,
    override val flows: FlowConfig,
    override var categories: CategoryConfig,
    override val bankAccountNumberLength: Int,
) : InstitutionConfig {

    companion object {
        fun create(context: Context): LocalInstitutionConfig {
            val preferences by lazy { context.getSharedPreferences("JSON_STORAGE", 0) }
            val returnedList = getArrayList("institution_features", preferences)
            val transactionArray = arrayListOf<String>()
            if(returnedList!= null) {
                if (returnedList.contains("DPS")){
                    transactionArray.add("CashIn")
                }
                if (returnedList.contains("TWT")){
                    transactionArray.add("CashOut")
                }
                if (returnedList.contains("CWT")){
                    transactionArray.add("POSCashOut")
                }
                if (returnedList.contains("ATP")){
                    transactionArray.add("Recharge")
                }
                if (returnedList.contains("BPM")){
                    transactionArray.add("BillsPayment")
                }
                if (returnedList.contains("IFT")){
                    transactionArray.add("FundsTransferCommercialBank")
                }
                if (returnedList.contains("LFT")){
                    transactionArray.add("LocalFundsTransfer")
                }
                if (returnedList.contains("COL")){
                    transactionArray.add("CollectionPayment")
                }
                if (returnedList.contains("IBTW")){
                    transactionArray.add("CrossBankTokenWithdrawal")
                }
            }
            else {
                transactionArray.add("Nothing")
            }
            val resources = context.resources
            val config = LocalInstitutionConfig(
                hasOnlineFunctions = true,
                hasHlaTagging = resources.getBoolean(R.bool.hla_enabled),
                name = resources.getString(R.string.institution_name),
                categories = CategoryConfig(
                    loans = resources.getBoolean(R.bool.category_loan),
                    customers = resources.getBoolean(R.bool.category_customer),
                    subscriptions = true,
                ),
                transactionTypes = transactionArray.map {
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

                if (resources.getBoolean(R.bool.flow_get_a_loan)) {
                    getALoan = Any()
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