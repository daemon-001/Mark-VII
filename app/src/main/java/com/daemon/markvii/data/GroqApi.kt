package com.daemon.markvii.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Groq API Data Models
 * Groq uses the same OpenAI-compatible request/response format as OpenRouter.
 * We reuse OpenRouterRequest, Message, Content, Choice, MessageResponse,
 * and ErrorResponse from OpenRouterApi.kt.
 */

data class GroqModelsResponse(
    val `object`: String?,
    val data: List<GroqModelData>?
)

data class GroqModelData(
    val id: String,
    val `object`: String?,
    val created: Long?,
    val owned_by: String?,
    val active: Boolean?,
    val context_window: Int?,
    val public_apps: Any?
)

/**
 * Groq Retrofit API Interface (OpenAI-compatible)
 */
interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: OpenRouterRequest): OpenRouterResponse

    @POST("chat/completions")
    suspend fun chatCompletionStream(@Body request: OpenRouterRequest): okhttp3.ResponseBody

    @GET("models")
    suspend fun getModels(): GroqModelsResponse
}

/**
 * Groq API Client
 */
object GroqClient {

    private const val BASE_URL = "https://api.groq.com/openai/v1/"

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
                    if (apiKey.isNotEmpty()) {
                        addHeader("Authorization", "Bearer $apiKey")
                    }
                }
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

    val api: GroqApiService = retrofit.create(GroqApiService::class.java)

    /**
     * Verify API key by hitting the /models endpoint (requires auth)
     */
    suspend fun verifyKey(keyToVerify: String): Boolean {
        if (keyToVerify.isBlank()) return false

        return try {
            val request = okhttp3.Request.Builder()
                .url(BASE_URL + "models")
                .addHeader("Authorization", "Bearer $keyToVerify")
                .get()
                .build()

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
