package com.creditclub.core.data.api

import java.io.IOException

class RequestFailureException(
    override val message: String,
    val httpStatusCode: Int? = null
) : IOException(message)