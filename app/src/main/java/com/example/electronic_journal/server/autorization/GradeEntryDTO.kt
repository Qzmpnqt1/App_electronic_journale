package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GradeEntryDTO(
    @SerializedName("entryId")
    val entryId: Int,

    @SerializedName("subjectName")
    val subjectName: String,

    @SerializedName("winterGrade")
    val winterGrade: Int?,             // null, если не выставлена
    @SerializedName("winterDateAssigned")
    val winterDateAssigned: String?,   // или LocalDateTime в зависимости от парсинга

    @SerializedName("summerGrade")
    val summerGrade: Int?,             // null, если не выставлена
    @SerializedName("summerDateAssigned")
    val summerDateAssigned: String?    // или LocalDateTime
)
