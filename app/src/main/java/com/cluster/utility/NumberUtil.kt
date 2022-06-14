package com.cluster.utility

import kotlin.math.roundToInt

fun Double.roundTo2dp() =
    (this * 100.0).roundToInt()/100.0