package com.example.electronic_journal.server

import android.content.Context
import com.example.electronic_journal.server.service.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

object WebServerSingleton {
    private const val WEB_SRV_URL = "http://192.168.0.91:8080/" // Проверьте адрес

    fun getApiService(context: Context): ApiService {
        // Создаем перехватчик для авторизации
        val authInterceptor = AuthInterceptor(context)

        // Создаем OkHttpClient с перехватчиками
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)  // Перехватчик для добавления токена
            .build()

        // Строим Retrofit с использованием OkHttpClient
        val retrofit = Retrofit.Builder()
            .baseUrl(WEB_SRV_URL)
            .client(okHttpClient)  // Используем OkHttpClient с перехватчиками
            .addConverterFactory(GsonConverterFactory.create()) // Для конвертации JSON в объекты
            .build()

        // Создаем ApiService
        return retrofit.create(ApiService::class.java)
    }
}

