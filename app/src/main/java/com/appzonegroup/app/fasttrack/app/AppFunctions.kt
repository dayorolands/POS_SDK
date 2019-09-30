package com.appzonegroup.app.fasttrack.app

import com.appzonegroup.app.fasttrack.R


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/27/2019.
 * Appzone Ltd
 */


object AppFunctions {
    const val REGISTER = 0
    const val DEPOSIT = 1
    const val TOKEN_WITHDRAWAL = 2
    const val CARD_WITHDRAWAL = 3
    const val LOAN_REQUEST = 4
    const val BVN_UPDATE = 5
    const val CUSTOMER_BALANCE_ENQUIRY = 6
    const val AGENT_BALANCE_ENQUIRY = 7
    const val PAY_BILL = 8
    const val CUSTOMER_CHANGE_PIN = 8
    const val AGENT_CHANGE_PIN = 9
    const val CUSTOMER_MINI_STATEMENT = 10
    const val TELLER_MINI_STATEMENT = 11

    private val FUNCTION_MAP = mutableListOf<AppFunction>()

    operator fun get(id: Int) = FUNCTION_MAP[id]

    init {
        FUNCTION_MAP.add(REGISTER, AppFunction(R.id.register_button, R.drawable.payday_loan, "Open Account"))
    }

    class AppFunction(val id: Int, val icon: Int, val name: String)

    object Categories {
        const val CUSTOMER_CATEGORY = 0
        const val LOAN_CATEGORY = 1
        const val AGENT_CATEGORY = 2
        const val TRANSACTIONS_CATEGORY = 3

        private val CATEGORIES = listOf("Customer", "Loans", "Agent", "Transactions")

        operator fun get(index: Int) = CATEGORIES[index]
    }
}