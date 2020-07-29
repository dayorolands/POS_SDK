package com.appzonegroup.creditclub.pos.provider.qpos

import com.appzonegroup.creditclub.pos.card.CardTransactionStatus


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 11/12/2019.
 * Appzone Ltd
 */
object MPosErrorCode {
    operator fun get(index: Int): Int {
        return when (index) {
            -31 -> CardTransactionStatus.UserCancel.code
            else -> index
        }
    }
}