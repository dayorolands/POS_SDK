package com.creditclub.conversation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf


@Immutable
data class Message(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null
)
