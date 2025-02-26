package com.example.electronic_journal.server

import android.content.Context
import com.example.electronic_journal.ai.MistralApiService
import com.example.electronic_journal.server.service.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val requestBuilder = chain.request().newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

class MistralAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Authorization", "Bearer Ps9HteRpwJT2sVPNjTWo0VHCAZ8PmpcV")
        return chain.proceed(requestBuilder.build())
    }
}

object WebServerSingleton {
    private const val WEB_SRV_URL = "http://192.168.0.75:8080/" // Проверьте адрес

    fun getApiService(context: Context): ApiService {
        val authInterceptor = AuthInterceptor(context)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(WEB_SRV_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    fun getMistralApiService(context: Context): MistralApiService {
        val mistralAuthInterceptor = MistralAuthInterceptor()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(mistralAuthInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // Увеличиваем время ожидания подключения
            .readTimeout(30, TimeUnit.SECONDS)    // Увеличиваем время ожидания чтения
            .writeTimeout(30, TimeUnit.SECONDS)   // Увеличиваем время ожидания записи
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mistral.ai/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(MistralApiService::class.java)
    }
}
