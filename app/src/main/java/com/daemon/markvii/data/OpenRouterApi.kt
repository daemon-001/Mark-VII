package com.daemon.markvii.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import org.json.JSONObject
import java.util.concurrent.TimeUnit

import androidx.annotation.Keep
import com.daemon.markvii.BuildConfig
import com.google.gson.annotations.SerializedName

/**
 * OpenRouter API Data Models
 */
@Keep
data class OpenRouterKeyInfo(
    @SerializedName("label") val label: String?,
    @SerializedName("usage") val usage: Double,
    @SerializedName("limit") val limit: Double?,
    @SerializedName("is_free_tier") val isFreeTier: Boolean
)

@Keep
data class OpenRouterRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("max_tokens") val max_tokens: Int = 1000,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("stream") val stream: Boolean = false
)

@Keep
data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: List<Content>
)

@Keep
data class Content(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_url") val image_url: ImageUrl? = null
)

@Keep
data class ImageUrl(
    @SerializedName("url") val url: String
)

@Keep
data class OpenRouterResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("choices") val choices: List<Choice>?,
    @SerializedName("error") val error: ErrorResponse?
)

@Keep
data class Choice(
    @SerializedName("message") val message: MessageResponse,
    @SerializedName("finish_reason") val finish_reason: String?
)

@Keep
data class MessageResponse(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

@Keep
data class ErrorResponse(
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String?,
    @SerializedName("code") val code: String?
)

/**
 * Models API Response Data Models
 */
@Keep
data class OpenRouterModelsResponse(
    @SerializedName("data") val data: List<ModelData>?
)

@Keep
data class ModelData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("created") val created: Long?,
    @SerializedName("pricing") val pricing: ModelPricing?,
    @SerializedName("context_length") val context_length: Int?,
    @SerializedName("architecture") val architecture: ModelArchitecture?,
    @SerializedName("top_provider") val top_provider: ModelProvider?
)

@Keep
data class ModelPricing(
    @SerializedName("prompt") val prompt: String?,
    @SerializedName("completion") val completion: String?,
    @SerializedName("request") val request: String?,
    @SerializedName("image") val image: String?
)

@Keep
data class ModelArchitecture(
    @SerializedName("modality") val modality: String?,
    @SerializedName("tokenizer") val tokenizer: String?,
    @SerializedName("instruct_type") val instruct_type: String?
)

@Keep
data class ModelProvider(
    @SerializedName("context_length") val context_length: Int?,
    @SerializedName("max_completion_tokens") val max_completion_tokens: Int?,
    @SerializedName("is_moderated") val is_moderated: Boolean?
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
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
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
        
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
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
                    
                val response = client.newCall(request).execute()
                val isSuccess = response.isSuccessful
                response.close()
                isSuccess
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Get API key usage statistics
     */
    suspend fun getKeyUsage(key: String): OpenRouterKeyInfo? {
        if (key.isBlank()) return null
        
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val request = okhttp3.Request.Builder()
                    .url(BASE_URL + "auth/key")
                    .addHeader("Authorization", "Bearer $key")
                    .get()
                    .build()
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                    
                val response = client.newCall(request).execute()
                val isSuccess = response.isSuccessful
                val responseBody = response.body?.string()
                response.close()
                
                if (isSuccess && responseBody != null) {
                    val jsonObject = JSONObject(responseBody).getJSONObject("data")
                    val label = jsonObject.optString("label", "Unknown Key")
                    val usage = jsonObject.optDouble("usage", 0.0)
                    val limit = if (jsonObject.isNull("limit")) null else jsonObject.optDouble("limit")
                    val isFreeTier = jsonObject.optBoolean("is_free_tier", false)
                    
                    OpenRouterKeyInfo(label, usage, limit, isFreeTier)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

