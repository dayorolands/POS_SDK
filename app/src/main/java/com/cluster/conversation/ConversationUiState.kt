package com.cluster.conversation

import androidx.compose.runtime.Immutable


@Immutable
data class Message(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null
)
