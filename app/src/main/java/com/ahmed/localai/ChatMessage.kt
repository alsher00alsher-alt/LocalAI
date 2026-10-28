package com.ahmed.localai

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val thinking: String = ""
)
