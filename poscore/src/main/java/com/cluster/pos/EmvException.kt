package com.cluster.pos

import java.lang.RuntimeException

class EmvException(override val message: String = "") : RuntimeException(message)