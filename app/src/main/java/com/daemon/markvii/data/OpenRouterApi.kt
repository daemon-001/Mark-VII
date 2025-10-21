package com.daemon.markvii.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * OpenRouter API Data Models
 */
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: List<Content>
)

data class Content(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class OpenRouterResponse(
    val id: String?,
    val model: String?,
    val choices: List<Choice>?,
    val error: ErrorResponse?
)

data class Choice(
    val message: MessageResponse,
    val finish_reason: String?
)

data class MessageResponse(
    val role: String,
    val content: String
)

data class ErrorResponse(
    val message: String,
    val type: String?,
    val code: String?
)

/**
 * Retrofit API Interface
 */
interface OpenRouterApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: OpenRouterRequest): OpenRouterResponse
}

/**
 * OpenRouter API Client
 */
object OpenRouterClient {
    
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    
    // API key loaded ONLY from Firebase
    private var apiKey: String = ""
    
    fun updateApiKey(newKey: String) {
        if (newKey.isNotEmpty()) {
            apiKey = newKey
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://github.com/daemon-001/Mark-VII")
                .addHeader("X-Title", "Mark-VII")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: OpenRouterApiService = retrofit.create(OpenRouterApiService::class.java)
}

