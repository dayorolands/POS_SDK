package com.creditclub.pos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.creditclub.pos.card.AccountType
import com.creditclub.pos.card.CardReaderEvent

class PosTransactionViewModel : ViewModel() {
    val amountString = MutableLiveData("0")
    val longAmount = MutableLiveData(0L)
    val amount = MutableLiveData(0.00)
    val amountCurrencyFormat = MutableLiveData("NGN0.00")
    val amountNumberFormat = MutableLiveData("0.00")
    val accountType = MutableLiveData<AccountType>()
    val cardReaderEvent = MutableLiveData<CardReaderEvent>()

    private val nextOperation = MutableLiveData<Function0<Unit>>()

    fun setNextOperation(block: Function0<Unit>) {
        nextOperation.value = block
    }

    fun next() {
        val function = nextOperation.value
        nextOperation.value = null
        function?.invoke()
    }

    fun clearAmount() {
        amountString.value = "0"
    }
}