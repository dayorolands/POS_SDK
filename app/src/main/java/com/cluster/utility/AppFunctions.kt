package com.cluster.utility

import com.cluster.R
import com.cluster.core.AppFunction
import com.cluster.core.AppFunctions


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 22/10/2019.
 * Appzone Ltd
 */

object FunctionIds {
    const val ACCOUNT_OPENING = 0
    const val NEW_WALLET = 1
    const val DEPOSIT = 2
    const val TOKEN_WITHDRAWAL = 3
    const val LOAN_REQUEST = 4
    const val BVN_UPDATE = 5
    const val CUSTOMER_BALANCE_ENQUIRY = 6
    const val AGENT_BALANCE_ENQUIRY = 7
    const val PAY_BILL = 8
    const val CUSTOMER_CHANGE_PIN = 9
    const val AGENT_CHANGE_PIN = 10
    const val CUSTOMER_MINI_STATEMENT = 11
    const val AGENT_MINI_STATEMENT = 12
    const val AIRTIME_RECHARGE = 14
    const val SUPPORT = 15
    const val CARD_TRANSACTIONS = 16
    const val FUNDS_TRANSFER = 17
    const val HLA_TAGGING = 18
    const val FAQS = 19
    const val COLLECTION_PAYMENT = 20
    const val USSD_WITHDRAWAL = 21
    const val PENDING_TRANSACTIONS = 22
    const val CHANGE_PASSWORD = 23
    const val CARDLESS_TOKEN = 24
}

fun registerAppFunctions() {
    AppFunctions.register(FunctionIds.ACCOUNT_OPENING, AppFunction(R.id.register_button, R.string.account_opening, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.NEW_WALLET, AppFunction(R.id.new_wallet_button, R.string.title_activity_new_wallet, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.DEPOSIT, AppFunction(R.id.deposit_button, R.string.cash_deposit, R.drawable.deposit))
    AppFunctions.register(FunctionIds.CARDLESS_TOKEN, AppFunction(R.id.fn_token_withdrawal, R.string.withdrawal_by_token,R.drawable.withdrawal))
    AppFunctions.register(FunctionIds.TOKEN_WITHDRAWAL, AppFunction(R.id.token_withdrawal_button, R.string.withdrawal_by_token, R.drawable.withdrawal))
    AppFunctions.register(FunctionIds.LOAN_REQUEST, AppFunction(R.id.loan_request_button, R.string.loan_request, R.drawable.personal_income))
    AppFunctions.register(FunctionIds.BVN_UPDATE, AppFunction(R.id.bvn_update_button, R.string.title_activity_bvnUpdate, R.drawable.secured_loan))
    AppFunctions.register(FunctionIds.CUSTOMER_BALANCE_ENQUIRY, AppFunction(R.id.customer_balance_enquiry_button, R.string.customer_balance, R.drawable.income))
    AppFunctions.register(FunctionIds.AGENT_BALANCE_ENQUIRY, AppFunction(R.id.agent_balance_enquiry_button, R.string.agent_s_balance, R.drawable.income))
    AppFunctions.register(FunctionIds.PAY_BILL, AppFunction(R.id.fn_bill_payment, R.string.pay_a_bill, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.CUSTOMER_CHANGE_PIN, AppFunction(R.id.customer_change_pin_button, R.string.change_customer_pin, R.drawable.login_password))
    AppFunctions.register(FunctionIds.AGENT_CHANGE_PIN, AppFunction(R.id.agent_change_pin_button, R.string.pin_change, R.drawable.login_password))
    AppFunctions.register(FunctionIds.CUSTOMER_MINI_STATEMENT, AppFunction(R.id.customer_mini_statement_button, R.string.title_activity_basic_mini_statement, R.drawable.deposit))
    AppFunctions.register(FunctionIds.AGENT_MINI_STATEMENT, AppFunction(R.id.agent_mini_statement_button, R.string.title_activity_basic_mini_statement, R.drawable.deposit))
    AppFunctions.register(FunctionIds.AIRTIME_RECHARGE, AppFunction(R.id.airtime_button, R.string.airtimetopup, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.SUPPORT, AppFunction(R.id.fn_support, R.string.title_activity_support, R.drawable.ic_chat_bubble_outline))
    AppFunctions.register(FunctionIds.CARD_TRANSACTIONS, AppFunction(R.id.card_withdrawal_button, R.string.pos_card_transactions, R.drawable.withdrawal))
    AppFunctions.register(FunctionIds.FUNDS_TRANSFER, AppFunction(R.id.funds_transfer_button, R.string.fund_stransfer, R.drawable.deposit))
    AppFunctions.register(FunctionIds.HLA_TAGGING, AppFunction(R.id.fn_hla_tagging, R.string.hla_tagging, R.drawable.ic_maps_and_flags))
    AppFunctions.register(FunctionIds.FAQS, AppFunction(R.id.fn_faq, R.string.title_activity_faq, R.drawable.ic_help))
    AppFunctions.register(FunctionIds.COLLECTION_PAYMENT, AppFunction(R.id.fn_collections_payment, R.string.title_fragment_collection_payment, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.USSD_WITHDRAWAL, AppFunction(R.id.ussd_withdrawal_button, R.string.ussd_withdrawal, R.drawable.withdrawal))
    AppFunctions.register(
        FunctionIds.PENDING_TRANSACTIONS,
        AppFunction(
            R.id.fn_pending_transactions,
            R.string.pending_transactions,
            R.drawable.ic_baseline_hourglass_bottom_24
        )
    )
    AppFunctions.register(
        FunctionIds.CHANGE_PASSWORD,
        AppFunction(
            R.id.change_password_button,
            R.string.change_password,
            R.drawable.login_password
        )
    )
}