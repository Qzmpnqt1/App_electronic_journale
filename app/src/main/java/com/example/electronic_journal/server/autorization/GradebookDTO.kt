package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GradebookDTO(
    @SerializedName("gradebookId")
    val gradebookId: Int,

    @SerializedName("studentId")
    val studentId: Int,

    @SerializedName("gradeEntries")
    val gradeEntries: List<GradeEntryDTO>
)