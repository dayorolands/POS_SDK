package com.creditclub.core

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/27/2019.
 * Appzone Ltd
 */

object AppFunctions {

    private val FUNCTION_MAP = hashMapOf<Int, AppFunction>()

    operator fun get(id: Int) = FUNCTION_MAP[id]

    fun register(id: Int, appFunction: AppFunction) {
        FUNCTION_MAP[id] = appFunction
    }

    object Categories {
        const val CUSTOMER_CATEGORY = 0
        const val LOAN_CATEGORY = 1
        const val AGENT_CATEGORY = 2
        const val TRANSACTIONS_CATEGORY = 3

        private val CATEGORIES = listOf("Customer", "Loans", "Agent", "Transactions")

        operator fun get(index: Int) = CATEGORIES[index]
    }
}