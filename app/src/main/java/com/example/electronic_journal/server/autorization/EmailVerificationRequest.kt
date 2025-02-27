package com.example.electronic_journal.server.autorization

data class EmailVerificationRequest(
    val email: String,
    val code: String
)
