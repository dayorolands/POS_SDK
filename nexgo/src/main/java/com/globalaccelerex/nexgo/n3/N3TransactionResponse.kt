package com.globalaccelerex.nexgo.n3

import com.creditclub.pos.TransactionResponse

data class N3TransactionResponse(override val code: String) : TransactionResponse {
    override val responseMessage
        get() = getResponseMessage(code)
}

fun getResponseMessage(code: String) = when (code) {
    "00" -> "SUCCESS"
    "02" -> "FAILED"
    "03" -> "CANCEL"
    "04" -> "INVALID FORMAT"
    "05" -> "WRONG PARAMETER"
    "06" -> "TIMEOUT"
    "09" -> "ACTIVITY CANCELLED"
    else -> "An error occurred"
}