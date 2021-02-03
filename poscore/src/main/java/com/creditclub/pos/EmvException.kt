package com.creditclub.pos

import java.lang.RuntimeException

class EmvException(override val message: String = "") : RuntimeException(message)