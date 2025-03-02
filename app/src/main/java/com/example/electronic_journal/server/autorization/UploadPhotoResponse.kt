package com.example.electronic_journal.server.autorization

import com.google.gson.annotations.SerializedName

data class UploadPhotoResponse(
    @SerializedName("photoUrl")
    val photoUrl: String
)
