package com.creditclub.core.type

import android.content.Context
import androidx.annotation.StringRes
import com.creditclub.core.R


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */
enum class PaymentStatus(
    val code: Int,
    val label: String, @StringRes val labelRes: Int = R.string.please_replace
) {
    Unpaid(0, "Unpaid"),
    Pending(1, "Pending"),
    Successful(2, "Successful"),
    Failed(3, "Failed");

    fun label(context: Context): String = context.getString(labelRes)

    companion object {

        fun find(index: Int): PaymentStatus {

            return values().find { it.code == index }
                ?: throw IndexOutOfBoundsException("No transaction status type exists for code $index")
        }
    }
}