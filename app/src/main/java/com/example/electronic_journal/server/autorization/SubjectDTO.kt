package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName

data class SubjectDTO(
    @SerializedName("name")
    var name: String,

    @SerializedName("course")
    var course: Int
)