package com.example.electronic_journal.ai

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface MistralApiService {
    @POST("v1/chat/completions")
    suspend fun completeChat(@Body requestBody: RequestBody): GeneralResponse
}
