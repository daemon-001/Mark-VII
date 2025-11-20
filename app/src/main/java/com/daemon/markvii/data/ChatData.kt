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

/**
 * Configuration class for all AI models via OpenRouter
 * Models are now loaded ONLY from Firebase - no hardcoded models
 */
object ModelConfiguration {
    // Empty list - all models must come from Firebase
    // NO hardcoded models - app requires Firebase configuration
    val models = emptyList<ModelInfo>()
}

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
                throw Exception("API_KEY_MISSING|⚠️ API Key Missing!\n\nPlease configure Firebase:\n1. Add OpenRouter API key to Firebase\n2. Field: openrouterApiKey\n3. Location: app_config/api_keys\n\nSee TROUBLESHOOTING_401_ERROR.md")
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
                        "HTTP_401|⚠️ HTTP 401: Unauthorized\n\nYour API key is invalid or missing.\n\nSolution:\n1. Get key from: https://openrouter.ai/keys\n2. Add to Firebase: app_config/api_keys\n3. Field: openrouterApiKey\n\nSee TROUBLESHOOTING_401_ERROR.md"
                    response.error.message.contains("404") || response.error.message.contains("Not Found") ->
                        "HTTP_404|⚠️ HTTP 404: Model Not Found\n\nThe model '$modelToUse' doesn't exist.\n\nSolution:\n1. Check model name in Firebase\n2. Use valid OpenRouter model\n3. See available models: https://openrouter.ai/models\n\nCommon models:\n- anthropic/claude-3-5-sonnet-20241022\n- openai/gpt-4-turbo\n- openai/gpt-3.5-turbo"
                    else -> "API_ERROR|Error: ${response.error.message}"
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
                    "HTTP_401|⚠️ HTTP 401: Unauthorized\n\nYour OpenRouter API key is invalid.\n\nSteps to fix:\n1. Go to https://openrouter.ai/keys\n2. Create/copy your API key\n3. Add to Firebase: app_config/api_keys/openrouterApiKey\n4. Restart app\n\nSee TROUBLESHOOTING_401_ERROR.md for details"
                e.message?.contains("403") == true -> 
                    "HTTP_403|⚠️ HTTP 403: Forbidden\n\nCheck OpenRouter account credits"
                e.message?.contains("404") == true ->
                    "HTTP_404|⚠️ HTTP 404: Model Not Found\n\nThe selected model doesn't exist on OpenRouter.\n\nSolution:\n1. Update model names in Firebase\n2. Use valid OpenRouter models\n3. Check: https://openrouter.ai/models\n\nWorking models:\n- anthropic/claude-3-5-sonnet-20241022\n- openai/gpt-4-turbo-preview\n- openai/gpt-3.5-turbo"
                e.message?.contains("429") == true -> 
                    "HTTP_429|⚠️ Rate Limited\n\nToo many requests. Wait a moment and try again."
                else -> "NETWORK_ERROR|Network Error\n\nCouldn't connect to AI service.\n\nCheck:\n• Internet connection\n• Firebase configuration\n\nDetails: ${e.message ?: "Unknown error"}"
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
