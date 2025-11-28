package com.daemon.markvii.data

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.Content

/**
 * Gemini API client for direct Google AI integration
 */
object GeminiClient {
    
    private const val TAG = "GeminiClient"
    private var apiKey: String = ""
    private var currentModel: GenerativeModel? = null
    
    /**
     * Update API key and reinitialize model
     */
    fun updateApiKey(newKey: String) {
        apiKey = newKey
        currentModel = null
    }
    
    /**
     * Get or create model instance
     */
    private fun getModel(modelName: String): GenerativeModel {
        if (currentModel == null || apiKey.isEmpty()) {
            if (apiKey.isEmpty()) {
                throw Exception("API_KEY_MISSING|Gemini API key is not configured")
            }
            
            currentModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 8192  // Increased from 2048 to 8192
                }
            )
        }
        return currentModel!!
    }
    
    /**
     * Generate text response from prompt with conversation history
     */
    suspend fun generateContent(
        prompt: String, 
        modelName: String = "gemini-1.5-flash",
        conversationHistory: List<Chat> = emptyList()
    ): Chat {
        try {
            val model = getModel(modelName)
            
            // If there's conversation history, use chat mode
            val response = if (conversationHistory.isNotEmpty()) {
                val history = conversationHistory
                    .filter { !it.isError } // Exclude error messages
                    .takeLast(10) // Limit to last 10 messages to avoid token limits
                    .map { chat ->
                        content(role = if (chat.isFromUser) "user" else "model") {
                            text(chat.prompt)
                        }
                    }
                
                val chat = model.startChat(history = history)
                chat.sendMessage(prompt)
            } else {
                model.generateContent(prompt)
            }
            
            val responseText = response.text ?: "No response generated"
            
            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelName
            )
        } catch (e: Exception) {
            // Format error for consistent handling
            val errorMessage = when {
                e.message?.contains("API key", ignoreCase = true) == true -> 
                    "API_KEY_INVALID|Invalid or expired Gemini API key"
                e.message?.contains("quota", ignoreCase = true) == true -> 
                    "QUOTA_EXCEEDED|Gemini API quota exceeded"
                e.message?.contains("safety", ignoreCase = true) == true -> 
                    "CONTENT_BLOCKED|Content blocked by Gemini safety filters"
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "NETWORK_ERROR|Network error: ${e.message}"
                else -> "GEMINI_ERROR|${e.message ?: "Unknown error occurred"}"
            }
            throw Exception(errorMessage)
        }
    }
    
    /**
     * Generate streaming response from prompt with conversation history
     */
    suspend fun generateContentStream(
        prompt: String, 
        modelName: String = "gemini-1.5-flash",
        conversationHistory: List<Chat> = emptyList(),
        onChunk: (String) -> Unit,
        onFinish: (finishReason: String?) -> Unit = {}
    ) {
        try {
            val model = getModel(modelName)
            
            // If there's conversation history, use chat mode
            val response = if (conversationHistory.isNotEmpty()) {
                val history = conversationHistory
                    .filter { !it.isError } // Exclude error messages
                    .takeLast(10) // Limit to last 10 messages
                    .map { chat ->
                        content(role = if (chat.isFromUser) "user" else "model") {
                            text(chat.prompt)
                        }
                    }
                
                val chat = model.startChat(history = history)
                chat.sendMessageStream(prompt)
            } else {
                model.generateContentStream(prompt)
            }
            
            var lastFinishReason: String? = null
            var buffer = StringBuilder()
            var lastEmitTime = System.currentTimeMillis()
            
            response.collect { chunk ->
                val text = chunk.text ?: ""
                if (text.isNotEmpty()) {
                    buffer.append(text)
                    val currentTime = System.currentTimeMillis()
                    
                    // Emit buffered text every 16ms (60fps) or when buffer reaches 3 characters for smooth typing effect
                    if (currentTime - lastEmitTime >= 16 || buffer.length >= 3) {
                        onChunk(buffer.toString())
                        buffer.clear()
                        lastEmitTime = currentTime
                    }
                }
                // Capture finish reason from the chunk
                chunk.candidates?.firstOrNull()?.finishReason?.let { reason ->
                    lastFinishReason = reason.toString()
                }
            }
            
            // Emit any remaining buffered text
            if (buffer.isNotEmpty()) {
                onChunk(buffer.toString())
            }
            
            // Notify about finish reason after streaming completes
            onFinish(lastFinishReason)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("API key", ignoreCase = true) == true -> 
                    "API_KEY_INVALID|Invalid or expired Gemini API key"
                e.message?.contains("quota", ignoreCase = true) == true -> 
                    "QUOTA_EXCEEDED|Gemini API quota exceeded"
                e.message?.contains("safety", ignoreCase = true) == true -> 
                    "CONTENT_BLOCKED|Content blocked by Gemini safety filters"
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "NETWORK_ERROR|Network error: ${e.message}"
                else -> "GEMINI_ERROR|${e.message ?: "Unknown error occurred"}"
            }
            throw Exception(errorMessage)
        }
    }
    
    /**
     * Generate content with image (multimodal)
     */
    suspend fun generateContentWithImage(
        prompt: String,
        bitmap: Bitmap,
        modelName: String = "gemini-1.5-flash"
    ): Chat {
        try {
            val model = getModel(modelName)
            
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            
            val response = model.generateContent(inputContent)
            val responseText = response.text ?: "No response generated"
            
            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelName
            )
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("API key", ignoreCase = true) == true -> 
                    "API_KEY_INVALID|Invalid or expired Gemini API key"
                e.message?.contains("quota", ignoreCase = true) == true -> 
                    "QUOTA_EXCEEDED|Gemini API quota exceeded"
                e.message?.contains("safety", ignoreCase = true) == true -> 
                    "CONTENT_BLOCKED|Content blocked by Gemini safety filters"
                e.message?.contains("image", ignoreCase = true) == true -> 
                    "IMAGE_ERROR|Error processing image: ${e.message}"
                else -> "GEMINI_ERROR|${e.message ?: "Unknown error occurred"}"
            }
            throw Exception(errorMessage)
        }
    }
}
