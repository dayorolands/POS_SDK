package com.globalaccelerex.nexgo.n3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosParameter
import com.creditclub.pos.TransactionResponse
import com.creditclub.pos.card.*
import com.creditclub.pos.printer.PosPrinter
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.dsl.module

class N3PosManager(val activity: CreditClubActivity) : PosManager, KoinComponent {
    override val cardReader = NexgoCardReader()
    override val sessionData = N3SessionData()

    override suspend fun loadEmv() {
        sessionData.reset()
    }

    override fun cleanUpEmv() {
        sessionData.reset()
    }

    override suspend fun openSettings(): Boolean {
        return try {
            val request = Intent("com.globalaccelerex.settings")
            val result = activity.getActivityResult(request)
            val status = result.data?.getStringExtra("status")
            true
        } catch (ex: Exception) {
            Log.e("PosManager", ex.message, ex)
            false
        }
    }

    override suspend fun openReprint(): Boolean {
        return try {
            val request = Intent("com.globalaccelerex.reprint")
            val result = activity.getActivityResult(request)
            val status = result.data?.getStringExtra("status")
            true
        } catch (ex: Exception) {
            Log.e("PosManager", ex.message, ex)
            false
        }
    }

    override suspend fun startTransaction(): TransactionResponse {
        val request = Intent("com.globalaccelerex.transaction")
        val amount = sessionData.amount.toDouble() / 100
        val intentTransType = sessionData.transactionType.intentTransType

        val jsonString =
            "{ \"transType\": \"${intentTransType}\", \"amount\":\"$amount\",\"print\":\"true\" }"

        request.putExtra("requestData", jsonString)
        val result = activity.getActivityResult(request)
        val status = result.data?.getStringExtra("status")
        status ?: throw IllegalArgumentException("Transaction status cannot be null")
        return N3TransactionResponse(status)
    }

    inner class N3SessionData : PosManager.SessionData() {
        override var canRunTransaction = true
        override var canManageParameters = false
        override var transactionType: TransactionType = TransactionType.Unknown
            set(value) {
                field = value
                canRunTransaction = field.isSupported
            }
    }

    inner class NexgoCardReader : CardReader {
        override suspend fun waitForCard(): CardReaderEvent {
            return CardReaderEvent.CHIP
        }

        override suspend fun read(amountStr: String): CardData? {
            return null
        }

        override fun endWatch() {

        }

        override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {

        }
    }

    private inline val TransactionType.intentTransType: String?
        get() = when (this) {
            TransactionType.Purchase -> "PURCHASE"
            TransactionType.CashBack -> "PURCHASEWITHCB"
            TransactionType.Balance -> "BALANCE"
            else -> null
        }

    private inline val TransactionType.isSupported: Boolean
        get() = when (this) {
            TransactionType.Purchase, TransactionType.CashBack, TransactionType.Balance -> true
            else -> false
        }

    private fun PosManager.SessionData.reset() {
        amount = 0L
        pinBlock = null
        canRunTransaction = true
        canManageParameters = false
        transactionType = TransactionType.Unknown
    }

    companion object : PosManagerCompanion {
        override fun setup(context: Context) {

        }

        override val module = module(override = true) {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                N3PosManager(activity)
            }
            factory<PosPrinter>(override = true) { (activity: Activity, dialogProvider: DialogProvider) ->
                N3Printer(activity, dialogProvider)
            }
            single<PosParameter>(override = true) { N3ParameterStore(androidContext()) }
        }

        override fun isCompatible(context: Context): Boolean {
            val intent = Intent("com.globalaccelerex.transaction")
            return intent.resolveActivity(context.packageManager) != null
        }
    }
}