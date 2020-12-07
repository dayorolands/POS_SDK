package com.creditclub.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
enum class CardTransactionStatus(val code: Int) {
    Success(1),
    Failure(0),
    UserCancel(-4),
    Timeout(-5),
    OfflinePinVerifyError(-32),
    NoPin(-11),
    CardRestricted(-6);

    companion object {
        fun find(code: Int) = values().find { it.code == code }
    }
}