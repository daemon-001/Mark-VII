package com.daemon.markvii.data

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * @author Nitesh
 */

/**
 * Data class to hold model information
 */
data class ModelInfo(
    val displayName: String,
    val apiModel: String,
    val isAvailable: Boolean = true
)

object ChatData {

    // API key loaded ONLY from Firebase - no local fallback
    var openrouter_api_key: String = ""

    var selected_model = ""

    /**
     * Update API key from Firebase
     * This is the ONLY way to set the API key
     */
    fun updateApiKey(newKey: String) {
        if (newKey.isNotEmpty()) {
            openrouter_api_key = newKey
            OpenRouterClient.updateApiKey(openrouter_api_key)
        }
    }
    
    /**
     * Fetch all available models from OpenRouter
     * Returns list of models with pricing info
     */
    suspend fun fetchAvailableModels(): List<ModelData> {
        return try {
            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.getModels()
            }
            response.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Fetch only FREE models from OpenRouter
     * A model is considered free if prompt and completion prices are "0"
     */
    suspend fun fetchFreeModels(): List<ModelInfo> {
        return try {
            val allModels = fetchAvailableModels()
            
            allModels.filter { model ->
                val pricing = model.pricing
                val promptPrice = pricing?.prompt?.toDoubleOrNull() ?: 1.0
                val completionPrice = pricing?.completion?.toDoubleOrNull() ?: 1.0
                
                // Free models have 0 cost for both prompt and completion
                promptPrice == 0.0 && completionPrice == 0.0
            }.mapIndexed { _, model ->
                // Clean up display name but keep original model ID
                val cleanDisplayName = (model.name ?: model.id)
                    .replace("(free)", "", ignoreCase = true)
                    .replace("  ", " ")
                    .trim()
                
                ModelInfo(
                    displayName = cleanDisplayName,
                    apiModel = model.id,
                    isAvailable = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Convert Bitmap to Base64 string for API
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun getResponse(prompt: String): Chat {
        try {
            // Check if API key is loaded
            if (openrouter_api_key.isEmpty()) {
                throw Exception("API_KEY_MISSING|API key is not configured")
            }
            
            // Use a valid default model if none selected
            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "anthropic/claude-3-5-sonnet-20241022" // Updated model name
            }
            
            val request = OpenRouterRequest(
                model = modelToUse,
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            Content(
                                type = "text",
                                text = prompt
                            )
                        )
                    )
                ),
                max_tokens = 2000,
                temperature = 0.7
            )

            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.chatCompletion(request)
            }

            // Check for errors
            if (response.error != null) {
                val errorMsg = when {
                    response.error.message.contains("401") || response.error.message.contains("Unauthorized") -> 
                        "HTTP_401|HTTP 401: Unauthorized - API key is invalid or missing"
                    response.error.message.contains("404") || response.error.message.contains("Not Found") ->
                        "HTTP_404|HTTP 404: Model Not Found - The model '$modelToUse' doesn't exist"
                    else -> "API_ERROR|${response.error.message}"
                }
                throw Exception(errorMsg)
            }

            // Get response text
            val responseText = response.choices?.firstOrNull()?.message?.content 
                ?: "No response received"

            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            // Re-throw if already formatted
            if (e.message?.contains("|") == true) {
                throw e
            }
            
            val errorMsg = when {
                e.message?.contains("401") == true -> 
                    "HTTP_401|HTTP 401: Unauthorized - OpenRouter API key is invalid"
                e.message?.contains("403") == true -> 
                    "HTTP_403|HTTP 403: Forbidden - Insufficient credits or permissions"
                e.message?.contains("404") == true ->
                    "HTTP_404|HTTP 404: Model Not Found - The selected model doesn't exist on OpenRouter"
                e.message?.contains("429") == true -> 
                    "HTTP_429|Rate Limited - Too many requests"
                else -> "NETWORK_ERROR|${e.message ?: "Unknown network error"}"
            }
            throw Exception(errorMsg)
        }
    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        try {
            // Convert bitmap to base64
            val base64Image = bitmapToBase64(bitmap)
            val dataUrl = "data:image/jpeg;base64,$base64Image"
            
            // Use a valid default model if none selected
            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "anthropic/claude-3-5-sonnet-20241022" // Updated model name
            }

            val request = OpenRouterRequest(
                model = modelToUse,
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            Content(
                                type = "image_url",
                                image_url = ImageUrl(url = dataUrl)
                            ),
                            Content(
                                type = "text",
                                text = prompt
                            )
                        )
                    )
                ),
                max_tokens = 2000,
                temperature = 0.7
            )

            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.chatCompletion(request)
            }

            // Check for errors
            if (response.error != null) {
                throw Exception("API_ERROR|Error: ${response.error.message}")
            }

            // Get response text
            val responseText = response.choices?.firstOrNull()?.message?.content 
                ?: "No response received"

            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            // Re-throw if already formatted
            if (e.message?.contains("|") == true) {
                throw e
            }
            
            throw Exception("NETWORK_ERROR|Model Offline: ${e.message ?: "Unknown error"}")
        }
    }
}
