package com.example.electronic_journal.server.model

import com.google.gson.annotations.SerializedName;
import kotlinx.serialization.Serializable

@Serializable
data class Teacher (
    @SerializedName("teacherId")
    var teacherId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("surname")
    val surname: String,

    @SerializedName("patronymic")
    val patronymic: String? = null,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    var role: String,

    @SerializedName("subjects")
    var subjects: Set<Subject> = emptySet()
)