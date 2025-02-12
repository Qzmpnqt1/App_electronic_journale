package com.example.electronic_journal.server.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GradeEntry(
    @SerializedName("entry_id")
    val entryId: Int,

    @SerializedName("gradebook_id")
    val gradebookId: Int,

    @SerializedName("subject_id")
    val subjectId: Int,

    @SerializedName("grade")
    val grade: Int
)
