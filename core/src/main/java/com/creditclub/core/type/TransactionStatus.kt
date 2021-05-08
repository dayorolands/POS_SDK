package com.creditclub.core.type

import android.content.Context
import androidx.annotation.StringRes
import com.creditclub.core.R


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */
enum class TransactionStatus(
    val code: Int,
    val label: String,
    @StringRes val labelRes: Int = R.string.please_replace
) {
    Pending(1, "Pending"),
    Failed(2, "Failed"),
    Successful(3, "Successful"),
    Reversed(4, "Reversed"),
    ThirdPartyFailure(5, "Third Party Failure");

    fun label(context: Context): String = context.getString(labelRes)

    companion object {

        fun find(index: Int): TransactionStatus {

            return values().find { it.code == index }
                ?: throw IndexOutOfBoundsException("No payment status type exists for code $index")
        }
    }
}