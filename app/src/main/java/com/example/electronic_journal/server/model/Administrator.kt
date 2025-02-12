package com.example.electronic_journal.server.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Administrator (

    @SerializedName("administrator_id")
    var administratorId: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("password")
    var password: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("role")
    var role: String
)