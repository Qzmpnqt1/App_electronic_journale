package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GradeEntryDTO(
    @SerializedName("entryId")
    val entryId: Int,

    @SerializedName("subjectName")
    val subjectName: String,

    @SerializedName("grade")
    val grade: Int
)