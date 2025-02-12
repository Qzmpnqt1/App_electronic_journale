package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
