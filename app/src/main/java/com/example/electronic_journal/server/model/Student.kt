package com.example.electronic_journal.server.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Student (
    @SerializedName("studentId")
    val studentId: Int,

    @SerializedName("group_id")
    val groupId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("surname")
    val surname: String,

    @SerializedName("patronymic")
    val patronymic: String? = null,

    @SerializedName("date_of_birth")
    val dateOfBirth: LocalDate? = null,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("photoUrl")
    val photoUrl: String? = null
)
