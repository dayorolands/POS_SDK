package com.creditclub.core

open class CreditClubException(override val message: String) : RuntimeException(message)

open class ValidationException(override val message: String) : CreditClubException(message)