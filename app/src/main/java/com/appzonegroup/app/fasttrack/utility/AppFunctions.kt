package com.appzonegroup.app.fasttrack.utility

import com.appzonegroup.app.fasttrack.R
import com.creditclub.core.AppFunction
import com.creditclub.core.AppFunctions


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
}

fun registerAppFunctions() {
    AppFunctions.register(FunctionIds.ACCOUNT_OPENING, AppFunction(R.id.register_button, R.string.account_opening, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.NEW_WALLET, AppFunction(R.id.new_wallet_button, R.string.title_activity_new_wallet, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.DEPOSIT, AppFunction(R.id.deposit_button, R.string.cash_deposit, R.drawable.deposit))
    AppFunctions.register(FunctionIds.TOKEN_WITHDRAWAL, AppFunction(R.id.token_withdrawal_button, R.string.withdrawal_by_token, R.drawable.withdrawal))
    AppFunctions.register(FunctionIds.LOAN_REQUEST, AppFunction(R.id.loan_request_button, R.string.loan_request, R.drawable.personal_income))
    AppFunctions.register(FunctionIds.BVN_UPDATE, AppFunction(R.id.bvn_update_button, R.string.title_activity_bvnUpdate, R.drawable.secured_loan))
    AppFunctions.register(FunctionIds.CUSTOMER_BALANCE_ENQUIRY, AppFunction(R.id.customer_balance_enquiry_button, R.string.customer_balance, R.drawable.income))
    AppFunctions.register(FunctionIds.AGENT_BALANCE_ENQUIRY, AppFunction(R.id.agent_balance_enquiry_button, R.string.agent_s_balance, R.drawable.income))
    AppFunctions.register(FunctionIds.PAY_BILL, AppFunction(R.id.pay_bill_button, R.string.pay_a_bill, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.CUSTOMER_CHANGE_PIN, AppFunction(R.id.customer_change_pin_button, R.string.change_customer_pin, R.drawable.login_password))
    AppFunctions.register(FunctionIds.AGENT_CHANGE_PIN, AppFunction(R.id.agent_change_pin_button, R.string.pin_change, R.drawable.login_password))
    AppFunctions.register(FunctionIds.CUSTOMER_MINI_STATEMENT, AppFunction(R.id.customer_mini_statement_button, R.string.title_activity_basic_mini_statement, R.drawable.deposit))
    AppFunctions.register(FunctionIds.AGENT_MINI_STATEMENT, AppFunction(R.id.agent_mini_statement_button, R.string.title_activity_basic_mini_statement, R.drawable.deposit))
    AppFunctions.register(FunctionIds.AIRTIME_RECHARGE, AppFunction(R.id.airtime_button, R.string.airtimetopup, R.drawable.payday_loan))
    AppFunctions.register(FunctionIds.SUPPORT, AppFunction(R.id.fn_support, R.string.title_activity_support, R.drawable.ic_logo_round))
    AppFunctions.register(FunctionIds.CARD_TRANSACTIONS, AppFunction(R.id.card_withdrawal_button, R.string.pos_card_transactions, R.drawable.withdrawal))
    AppFunctions.register(FunctionIds.FUNDS_TRANSFER, AppFunction(R.id.funds_transfer_button, R.string.fund_stransfer, R.drawable.deposit))
    AppFunctions.register(FunctionIds.HLA_TAGGING, AppFunction(R.id.fn_hla_tagging, R.string.hla_tagging, R.drawable.ic_maps_and_flags))
}