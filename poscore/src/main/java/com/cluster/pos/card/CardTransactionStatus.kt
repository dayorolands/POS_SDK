package com.cluster.pos.card


enum class CardTransactionStatus(val code: Int) {
    Success(1),
    Failure(0),
    UserCancel(-4),
    Timeout(-5),
    OfflinePinVerifyError(-32),
    NoPin(-11),
    CardRestricted(-6),
    CardExpired(-7),
    CardRemoved(-9),
    CardRestrictedDukpt(-10),
    Error(-1);

    companion object {
        fun find(code: Int) = values().find { it.code == code }
    }
}