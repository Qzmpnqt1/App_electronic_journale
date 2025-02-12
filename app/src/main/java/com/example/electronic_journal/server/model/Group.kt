package com.example.electronic_journal.server.model

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("groupId")
    var groupId: Int = 0,

    @SerializedName("name")
    var name: String = "",

    @SerializedName("subjects")
    var subjects: List<Subject> = emptyList()
)


