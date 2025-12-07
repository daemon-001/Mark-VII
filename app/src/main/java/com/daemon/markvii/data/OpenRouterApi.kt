package com.daemon.markvii.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
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
    val temperature: Double = 0.7,
    val stream: Boolean = false
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
 * Models API Response Data Models
 */
data class OpenRouterModelsResponse(
    val data: List<ModelData>?
)

data class ModelData(
    val id: String,
    val name: String?,
    val description: String?,
    val pricing: ModelPricing?,
    val context_length: Int?,
    val architecture: ModelArchitecture?,
    val top_provider: ModelProvider?
)

data class ModelPricing(
    val prompt: String?,
    val completion: String?,
    val request: String?,
    val image: String?
)

data class ModelArchitecture(
    val modality: String?,
    val tokenizer: String?,
    val instruct_type: String?
)

data class ModelProvider(
    val context_length: Int?,
    val max_completion_tokens: Int?,
    val is_moderated: Boolean?
)

/**
 * Retrofit API Interface
 */
interface OpenRouterApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: OpenRouterRequest): OpenRouterResponse
    
    @POST("chat/completions")
    suspend fun chatCompletionStream(@Body request: OpenRouterRequest): okhttp3.ResponseBody
    
    @GET("models")
    suspend fun getModels(): OpenRouterModelsResponse
}

/**
 * OpenRouter API Client
 */
object OpenRouterClient {
    
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    
    // API key loaded ONLY from Firebase
    private var apiKey: String = ""
    
    fun updateApiKey(newKey: String) {
        if (newKey.isNotBlank()) {
            apiKey = newKey.trim()
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val request = originalRequest.newBuilder()
                .apply {
                    // Only add Authorization header if API key exists and not calling /models endpoint
                    if (apiKey.isNotEmpty() && !originalRequest.url.encodedPath.endsWith("/models")) {
                        addHeader("Authorization", "Bearer $apiKey")
                    }
                }
                .addHeader("HTTP-Referer", "https://github.com/daemon-001/Mark-VII")
                .addHeader("X-Title", "Mark-VII")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: OpenRouterApiService = retrofit.create(OpenRouterApiService::class.java)

    /**
     * Verify API key validity
     */
    suspend fun verifyKey(keyToVerify: String): Boolean {
        if (keyToVerify.isBlank()) return false
        
        return try {
            val request = okhttp3.Request.Builder()
                .url(BASE_URL + "auth/key")
                .addHeader("Authorization", "Bearer $keyToVerify")
                .get()
                .build()
            
            // Use a new client to avoid interceptors interfering
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
                
            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }
            
            val isSuccess = response.isSuccessful
            response.close()
            isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

