package com.cluster.pos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cluster.pos.card.AccountType
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent

class PosTransactionViewModel : ViewModel() {
    val amountString = MutableLiveData("0")
    val cashbackString = MutableLiveData("0")
    val longAmount = MutableLiveData(0L)
    val amount = MutableLiveData(0.00)
    val preAuthStan = MutableLiveData("0")
    val amountCurrencyFormat = MutableLiveData("NGN0.00")
    val amountNumberFormat = MutableLiveData("0.00")
    val accountType = MutableLiveData<AccountType>()
    val cardReaderEvent = MutableLiveData<CardReaderEvent>()
    val cardData = MutableLiveData<CardData>()

    private val nextOperation = MutableLiveData<Function0<Unit>?>()

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