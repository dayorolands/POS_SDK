package com.creditclub.ui.util

import androidx.compose.runtime.saveable.listSaver
import java.time.Instant
import java.time.LocalDate

val LocalDateSaver = listSaver<LocalDate, String>(
    save = { listOf(it.toString()) },
    restore = { LocalDate.parse(it[0]) }
)

val InstantSaver = listSaver<Instant, String>(
    save = { listOf(it.toString()) },
    restore = { Instant.parse(it[0]) }
)