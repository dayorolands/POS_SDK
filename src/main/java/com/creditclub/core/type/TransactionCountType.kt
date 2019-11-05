package com.creditclub.core.type

/**
 * Created by Joseph on 1/6/2018.
 */

enum class TransactionCountType(val key: String) {
    REQUEST_COUNT("REQUEST_COUNT"),
    SUCCESS_COUNT("SUCCESS_COUNT"),
    NO_INTERNET_COUNT("NO_INTERNET_COUNT"),
    NO_RESPONSE_COUNT("NO_RESPONSE_COUNT"),
    ERROR_RESPONSE_COUNT("ERROR_RESPONSE_COUNT")
}
