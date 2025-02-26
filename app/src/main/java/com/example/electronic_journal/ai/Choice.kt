package com.example.electronic_journal.ai

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String,
    val logprobs: Any?
)
