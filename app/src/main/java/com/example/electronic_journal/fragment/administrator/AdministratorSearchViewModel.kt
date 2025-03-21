package com.example.electronic_journal.fragment.administrator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.electronic_journal.ai.GeneralRequest
import com.example.electronic_journal.ai.GeneralResponse
import com.example.electronic_journal.ai.Message
import com.example.electronic_journal.ai.MistralApiService
import com.example.electronic_journal.server.WebServerSingleton
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AdministratorSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _answerText = MutableLiveData<String>()
    val answerText: LiveData<String> = _answerText

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _lastQuery = MutableLiveData<String>()
    val lastQuery: LiveData<String> = _lastQuery

    // Новый флаг для отслеживания ошибки
    private val _errorOccurred = MutableLiveData<Boolean>()
    val errorOccurred: LiveData<Boolean> = _errorOccurred

    private val gson = Gson()
    private val mistralApiService: MistralApiService =
        WebServerSingleton.getMistralApiService(application)

    fun sendQuery(query: String) {
        _lastQuery.value = query
        _isLoading.value = true
        _errorOccurred.value = false

        viewModelScope.launch {
            try {
                val messages = listOf(Message(role = "user", content = query))
                val requestObj = GeneralRequest(model = "mistral-large-latest", messages = messages)
                val json = gson.toJson(requestObj)
                val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
                val response: GeneralResponse = mistralApiService.completeChat(requestBody)
                val content = response.choices.firstOrNull()?.message?.content ?: ""
                _answerText.value = if (content.isBlank()) {
                    "Нет результатов."
                } else {
                    "Ответ от Mistral:\n$content"
                }
            } catch (e: HttpException) {
                _answerText.value = "Ошибка запроса: ${e.code()} ${e.message()}"
                _errorOccurred.value = true
            } catch (e: Exception) {
                _answerText.value = "Ошибка: ${e.localizedMessage}"
                _errorOccurred.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
