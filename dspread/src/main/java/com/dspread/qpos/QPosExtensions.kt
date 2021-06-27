package com.dspread.qpos

import android.content.Context
import com.creditclub.pos.card.TransactionType
import com.dspread.R
import com.dspread.qpos.utils.TLV
import com.dspread.qpos.utils.TLVParser
import com.dspread.qpos.utils.hexBytes
import com.dspread.xpos.QPOSService
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/01/2020.
 * Appzone Ltd
 */

fun QPOSService.TransactionResult.getMessage(context: Context): String {
    return when (this) {
        QPOSService.TransactionResult.APPROVED -> {
            TRACE.d("TransactionResult.APPROVED")
            context.getString(R.string.transaction_approved)
        }
        QPOSService.TransactionResult.TERMINATED -> {
            context.getString(R.string.session_timeout)
        }
        QPOSService.TransactionResult.DECLINED -> {
            context.getString(R.string.transaction_declined)
        }
        QPOSService.TransactionResult.CANCEL -> {
            context.getString(R.string.transaction_cancel)
        }
        QPOSService.TransactionResult.CAPK_FAIL -> {
            context.getString(R.string.transaction_capk_fail)
        }
        QPOSService.TransactionResult.NOT_ICC -> {
            context.getString(R.string.transaction_not_icc)
        }
        QPOSService.TransactionResult.SELECT_APP_FAIL -> {
            context.getString(R.string.transaction_app_fail)
        }
        QPOSService.TransactionResult.DEVICE_ERROR -> {
            context.getString(R.string.transaction_device_error)
        }
        QPOSService.TransactionResult.TRADE_LOG_FULL -> {
            "the trade log has fulled!pls clear the trade log!"
        }
        QPOSService.TransactionResult.CARD_NOT_SUPPORTED -> {
            context.getString(R.string.card_not_supported)
        }
        QPOSService.TransactionResult.MISSING_MANDATORY_DATA -> {
            context.getString(R.string.missing_mandatory_data)
        }
        QPOSService.TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS -> {
            context.getString(R.string.card_blocked_or_no_evm_apps)
        }
        QPOSService.TransactionResult.INVALID_ICC_DATA -> {
            context.getString(R.string.invalid_icc_data)
        }
        QPOSService.TransactionResult.FALLBACK -> {
            "trans fallback"
        }
        QPOSService.TransactionResult.NFC_TERMINATED -> {
            "NFC Terminated"
        }
        QPOSService.TransactionResult.CARD_REMOVED -> {
            "CARD REMOVED"
        }
        else -> "Transaction Failed"
    }
}

inline val TransactionType.asQposTransactionType: QPOSService.TransactionType
    get() = when (this) {
        TransactionType.Purchase -> QPOSService.TransactionType.GOODS
        TransactionType.Unknown -> QPOSService.TransactionType.SERVICES
        TransactionType.CashAdvance -> QPOSService.TransactionType.GOODS
        TransactionType.Refund -> QPOSService.TransactionType.REFUND
        TransactionType.CashBack -> QPOSService.TransactionType.CASHBACK
        TransactionType.Reversal -> QPOSService.TransactionType.REFUND
        TransactionType.Balance -> QPOSService.TransactionType.INQUIRY
        TransactionType.SalesComplete -> QPOSService.TransactionType.SALE
        TransactionType.PreAuth -> QPOSService.TransactionType.PREAUTH
    }

fun List<TLV>.getTlv(tag: String): TLV? {
    return TLVParser.searchTLV(this, tag)
}

fun List<TLV>.getValue(
    tag: String,
    hex: Boolean = false,
    fpadded: Boolean = false
): String {
    val value = if (hex) String(getTlv(tag)?.value?.hexBytes ?: byteArrayOf())
    else getTlv(tag)?.value ?: ""

    if (fpadded) {
        val stringBuffer = StringBuffer(value)
        if (stringBuffer[stringBuffer.toString().length - 1] == 'F') {
            stringBuffer.deleteCharAt(stringBuffer.toString().length - 1)
        }
        return stringBuffer.toString().uppercase(Locale.getDefault())
    }

    return value.uppercase(Locale.getDefault())
}
