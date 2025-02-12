package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String
)
